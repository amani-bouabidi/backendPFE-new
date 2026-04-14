package com.ira.formation.services;

import com.ira.formation.dto.ProgressDTO;
import com.ira.formation.dto.ProgressForFormateurDTO;
import com.ira.formation.entities.*;
import com.ira.formation.repositories.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final UtilisateurRepository utilisateurRepository;
    private final FormationRepository formationRepository;
    private final ModuleRepository moduleRepository;
    private final ModuleCompletionRepository moduleCompletionRepository;
    private final ProgressRepository progressRepository;

    // =================== COMPLETE MODULE ===================
    public ProgressDTO completeModule(String email, Long formationId, Long moduleId) {

        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Formation formation = formationRepository.findById(formationId)
                .orElseThrow(() -> new RuntimeException("Formation non trouvée"));

        com.ira.formation.entities.Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module non trouvé"));

        // 🔐 check module appartient à formation
        if (!module.getFormation().getId().equals(formationId)) {
            throw new RuntimeException("Module n'appartient pas à cette formation");
        }

        // 🔥 éviter duplication
        if (!moduleCompletionRepository.existsByApprenantAndModule(user, module)) {

            ModuleCompletion mc = ModuleCompletion.builder()
                    .apprenant(user)
                    .module(module)
                    .completed(true)
                    .build();

            moduleCompletionRepository.save(mc);
        }

        return calculateProgress(user, formation);
    }

    // =================== GET PROGRESS ===================
    public ProgressDTO getMyProgress(String email, Long formationId) {

        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Formation formation = formationRepository.findById(formationId)
                .orElseThrow(() -> new RuntimeException("Formation non trouvée"));

        return calculateProgress(user, formation);
    }

    // =================== FORMATEUR VIEW ===================
    public List<ProgressForFormateurDTO> getProgressByFormation(String email, Long formationId) {

        Utilisateur formateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Formation formation = formationRepository.findById(formationId)
                .orElseThrow(() -> new RuntimeException("Formation non trouvée"));

        // 🔴 أهم سطر (الحماية)
        if (!formation.getFormateur().getId().equals(formateur.getId())) {
            throw new RuntimeException("Accès refusé: ce n'est pas votre formation");
        }

        List<Progress> progressList = progressRepository.findByFormation(formation);

        return progressList.stream().map(p ->
                ProgressForFormateurDTO.builder()
                        .apprenantId(p.getApprenant().getId())
                        .apprenantNom(p.getApprenant().getNom())
                        .apprenantPrenom(p.getApprenant().getPrenom())
                        .formationId(formation.getId())
                        .percentage(p.getPercentage())
                        .completed(p.getPercentage() >= 100)
                        .build()
        ).toList();
    }

    // =================== CORE LOGIC ===================
    private ProgressDTO calculateProgress(Utilisateur user, Formation formation) {

        long totalModules = moduleRepository.findByFormation(formation).size();

        if (totalModules == 0) {
            throw new RuntimeException("Pas de modules");
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