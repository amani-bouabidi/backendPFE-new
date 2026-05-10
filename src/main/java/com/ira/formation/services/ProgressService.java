package com.ira.formation.services;

import com.ira.formation.dto.ProgressDTO;
import com.ira.formation.dto.ProgressForAdminDTO;
import com.ira.formation.dto.ProgressForFormateurDTO;
import com.ira.formation.entities.Utilisateur;
import com.ira.formation.entities.Formation;
import com.ira.formation.entities.Module;
import com.ira.formation.entities.Progress;
import com.ira.formation.entities.ModuleCompletion;
import com.ira.formation.repositories.*;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final UtilisateurRepository utilisateurRepository;
    private final FormationRepository formationRepository;
    private final ModuleRepository moduleRepository;
    private final ModuleCompletionRepository moduleCompletionRepository;
    private final ProgressRepository progressRepository;
    private final NotificationService notificationService;
    private final InscriptionRepository inscriptionRepository;

    // =================== APPRENANT: COMPLETE MODULE ===================
    @Transactional
    public ProgressDTO completeModule(String email, Long formationId, Long moduleId) {

        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Formation formation = formationRepository.findById(formationId)
                .orElseThrow(() -> new RuntimeException("Formation non trouvée"));

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module non trouvé"));

        if (!module.getFormation().getId().equals(formationId)) {
            throw new RuntimeException("Module n'appartient pas à cette formation");
        }

        if (!moduleCompletionRepository.existsByApprenantAndModule(user, module)) {
            ModuleCompletion mc = ModuleCompletion.builder()
                    .apprenant(user)
                    .module(module)
                    .completed(true)
                    .build();
            moduleCompletionRepository.save(mc);
        }

        ProgressDTO progress = calculateProgress(user, formation);

        // ✅ Persister / mettre à jour dans la table progress
        Progress record = progressRepository
                .findByApprenantAndFormation(user, formation)
                .orElse(Progress.builder()
                        .apprenant(user)
                        .formation(formation)
                        .build());

        // ── Notification admins : UNE SEULE FOIS quand 100% est atteint ──
        boolean wasAlreadyCompleted = record.getPercentage() >= 100;
        record.setPercentage(progress.getPercentage());
        record.setLastModuleId(moduleId);
        record.setUpdatedAt(LocalDateTime.now());
        progressRepository.save(record);

        if (progress.isCompleted() && !wasAlreadyCompleted) {
            String msg = "🎓 L'apprenant " + user.getPrenom() + " " + user.getNom()
                    + " a atteint 100% dans la formation « "
                    + formation.getTitre() + " ». Il peut maintenant recevoir son attestation.";
            utilisateurRepository.findByRoleNom("ADMIN").forEach(admin ->
                notificationService.createNotification(admin, msg)
            );
        }

        // ── Retourner avec le moduleId correct ──────────────────────
        return ProgressDTO.builder()
                .formationId(formation.getId())
                .moduleId(moduleId)
                .percentage(progress.getPercentage())
                .completed(progress.isCompleted())
                .build();
    }

    // =================== APPRENANT: GET MY PROGRESS ===================
    public ProgressDTO getMyProgress(String email, Long formationId) {

        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Formation formation = formationRepository.findById(formationId)
                .orElseThrow(() -> new RuntimeException("Formation non trouvée"));

        ProgressDTO calc = calculateProgress(user, formation);

        // ✅ Lire le lastModuleId depuis la table progress (persisté par completeModule)
        Long lastModuleId = progressRepository
                .findByApprenantAndFormation(user, formation)
                .map(Progress::getLastModuleId)
                .orElse(null);

        return ProgressDTO.builder()
                .formationId(formation.getId())
                .moduleId(lastModuleId)
                .percentage(calc.getPercentage())
                .completed(calc.isCompleted())
                .build();
    }

    // =================== FORMATEUR: PROGRESSION PAR FORMATION ===================
    public List<ProgressForFormateurDTO> getProgressByFormation(String email, Long formationId) {

        Utilisateur formateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Formation formation = formationRepository.findById(formationId)
                .orElseThrow(() -> new RuntimeException("Formation non trouvée"));

        if (!formation.getFormateur().getId().equals(formateur.getId())) {
            throw new RuntimeException("Accès refusé: ce n'est pas votre formation");
        }

        // ✅ Tous les apprenants inscrits, calculé depuis module_completion
        return inscriptionRepository.findAll().stream()
                .filter(insc -> insc.getFormation().getId().equals(formationId))
                .map(insc -> {
                    Utilisateur apprenant = insc.getApprenant();
                    ProgressDTO calc = calculateProgress(apprenant, formation);
                    return ProgressForFormateurDTO.builder()
                            .apprenantId(apprenant.getId())
                            .apprenantNom(apprenant.getNom())
                            .apprenantPrenom(apprenant.getPrenom())
                            .formationId(formation.getId())
                            .percentage(calc.getPercentage())
                            .completed(calc.isCompleted())
                            .build();
                }).toList();
    }

    // =================== ADMIN: TOUTES LES PROGRESSIONS ===================
    @PreAuthorize("hasRole('ADMIN')")
    public List<ProgressForAdminDTO> getAllProgressForAdmin() {

        // ✅ Calculé en temps réel depuis module_completion (source de vérité)
        return inscriptionRepository.findAll().stream().map(insc -> {
            Utilisateur apprenant = insc.getApprenant();
            Formation formation = insc.getFormation();
            ProgressDTO calc = calculateProgress(apprenant, formation);
            return ProgressForAdminDTO.builder()
                    .apprenantId(apprenant.getId())
                    .apprenantNom(apprenant.getNom())
                    .apprenantPrenom(apprenant.getPrenom())
                    .apprenantEmail(apprenant.getEmail())
                    .formationId(formation.getId())
                    .formationTitre(formation.getTitre())
                    .formateurNom(formation.getFormateur() != null
                            ? formation.getFormateur().getNom() : "—")
                    .percentage(calc.getPercentage())
                    .completed(calc.isCompleted())
                    .build();
        }).toList();
    }

    // =================== ADMIN: PROGRESSIONS D'UNE FORMATION SPÉCIFIQUE ===================
    @PreAuthorize("hasRole('ADMIN')")
    public List<ProgressForAdminDTO> getProgressByFormationForAdmin(Long formationId) {

        Formation formation = formationRepository.findById(formationId)
                .orElseThrow(() -> new RuntimeException("Formation non trouvée"));

        // ✅ Tous les apprenants inscrits, calculé depuis module_completion
        return inscriptionRepository.findAll().stream()
                .filter(insc -> insc.getFormation().getId().equals(formationId))
                .map(insc -> {
                    Utilisateur apprenant = insc.getApprenant();
                    ProgressDTO calc = calculateProgress(apprenant, formation);
                    return ProgressForAdminDTO.builder()
                            .apprenantId(apprenant.getId())
                            .apprenantNom(apprenant.getNom())
                            .apprenantPrenom(apprenant.getPrenom())
                            .apprenantEmail(apprenant.getEmail())
                            .formationId(formation.getId())
                            .formationTitre(formation.getTitre())
                            .formateurNom(formation.getFormateur() != null
                                    ? formation.getFormateur().getNom() : "—")
                            .percentage(calc.getPercentage())
                            .completed(calc.isCompleted())
                            .build();
                }).toList();
    }

    // =================== ADMIN: PROGRESSIONS D'UN APPRENANT SPÉCIFIQUE ===================
    @PreAuthorize("hasRole('ADMIN')")
    public List<ProgressForAdminDTO> getProgressByApprenantForAdmin(Long apprenantId) {

        Utilisateur apprenant = utilisateurRepository.findById(apprenantId)
                .orElseThrow(() -> new RuntimeException("Apprenant non trouvé"));

        // ✅ Toutes les formations auxquelles l'apprenant est inscrit, calculé depuis module_completion
        return inscriptionRepository.findByApprenant(apprenant).stream()
                .map(insc -> {
                    Formation formation = insc.getFormation();
                    ProgressDTO calc = calculateProgress(apprenant, formation);
                    return ProgressForAdminDTO.builder()
                            .apprenantId(apprenant.getId())
                            .apprenantNom(apprenant.getNom())
                            .apprenantPrenom(apprenant.getPrenom())
                            .apprenantEmail(apprenant.getEmail())
                            .formationId(formation.getId())
                            .formationTitre(formation.getTitre())
                            .formateurNom(formation.getFormateur() != null
                                    ? formation.getFormateur().getNom() : "—")
                            .percentage(calc.getPercentage())
                            .completed(calc.isCompleted())
                            .build();
                }).toList();
    }
    // =================== CORE LOGIC ===================
    private ProgressDTO calculateProgress(Utilisateur user, Formation formation) {

        long totalModules = moduleRepository.findByFormation(formation).size();

        if (totalModules == 0) {
            return ProgressDTO.builder()
                    .formationId(formation.getId())
                    .moduleId(null)
                    .percentage(0.0)
                    .completed(false)
                    .build();
        }

        long completedModules =
                moduleCompletionRepository.countByApprenant_IdAndModule_Formation_IdAndCompletedTrue(
                        user.getId(),
                        formation.getId()
                );

        double percentage = ((double) completedModules / totalModules) * 100;

        return ProgressDTO.builder()
                .formationId(formation.getId())
                .moduleId(null)
                .percentage(percentage)
                .completed(percentage >= 100)
                .build();
    }

    // =================== APPRENANT: REPRENDRE PROGRESSION ===================
    public Long getLastModule(String email, Long formationId) {

        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Formation formation = formationRepository.findById(formationId)
                .orElseThrow(() -> new RuntimeException("Formation not found"));

        return progressRepository.findByApprenantAndFormation(user, formation)
                .map(Progress::getLastModuleId)
                .orElse(null);
    }
}