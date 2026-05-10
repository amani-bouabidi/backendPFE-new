package com.ira.formation.services;

import com.ira.formation.dto.*;
import com.ira.formation.entities.*;
import com.ira.formation.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FormationService {

    private final FormationRepository formationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final DomaineRepository domaineRepository;
    private final InscriptionRepository inscriptionRepository;
    private final NotificationService notificationService;
    private final TestRepository testRepository;
    private final SessionEnLigneRepository sessionRepository;

    // =========================================================
    // MAPPER SIMPLE (PUBLIC / LISTING)
    // =========================================================
    private FormationDTO map(Formation f) {
        return FormationDTO.builder()
                .id(f.getId())
                .titre(f.getTitre())
                .description(f.getDescription())
                .formateurId(f.getFormateur() != null ? f.getFormateur().getId() : null)
                .formateurNom(f.getFormateur() != null ? f.getFormateur().getNom() : null)
                .domaineId(f.getDomaine() != null ? f.getDomaine().getId() : null)
                .domaineNom(f.getDomaine() != null ? f.getDomaine().getNom() : null)
                .build();
    }

    // =========================================================
    // MAPPER FULL (CONTENU COMPLET: modules + documents + videos)
    // FIX N°2: includes formateurId, formateurNom, domaineId, domaineNom
    // =========================================================
    private FormationFullDTO mapFull(Formation f) {
        return FormationFullDTO.builder()
                .id(f.getId())
                .titre(f.getTitre())
                .description(f.getDescription())
                .formateurId(f.getFormateur() != null ? f.getFormateur().getId() : null)
                .formateurNom(f.getFormateur() != null ? f.getFormateur().getNom() + " " + f.getFormateur().getPrenom() : null)
                .domaineId(f.getDomaine() != null ? f.getDomaine().getId() : null)
                .domaineNom(f.getDomaine() != null ? f.getDomaine().getNom() : null)
                .modules(
                    f.getModules() == null ? Collections.emptyList() :
                        f.getModules().stream().map(m -> ModuleDTO.builder()
                            .id(m.getId())
                            .titre(m.getTitre())
                            .description(m.getDescription())
                            .documents(
                                m.getDocuments() == null ? Collections.emptyList() :
                                    m.getDocuments().stream().map(d -> DocumentDTO.builder()
                                        .id(d.getId())
                                        .nom(d.getNom())
                                        .filePath(d.getFilePath())
                                        .build()
                                    ).toList()
                            )
                            .videos(
                                m.getVideos() == null ? Collections.emptyList() :
                                    m.getVideos().stream().map(v -> VideoDTO.builder()
                                        .id(v.getId())
                                        .titre(v.getTitre())
                                        .filePath(v.getFilePath())
                                        .build()
                                    ).toList()
                            )
                            .build()
                        ).toList()
                )
                .build();
    }

    // =========================================================
    // ADMIN - CREATE (BUG FIX N°5: notify ALL apprenants)
    // =========================================================
    @PreAuthorize("hasRole('ADMIN')")
    public FormationDTO create(FormationDTO dto) {

        Utilisateur formateur = utilisateurRepository.findById(dto.getFormateurId())
                .orElseThrow(() -> new RuntimeException("Formateur introuvable"));

        Domaine domaine = domaineRepository.findById(dto.getDomaineId())
                .orElseThrow(() -> new RuntimeException("Domaine introuvable"));

        Formation f = Formation.builder()
                .titre(dto.getTitre())
                .description(dto.getDescription())
                .formateur(formateur)
                .domaine(domaine)
                .build();

        Formation saved = formationRepository.save(f);

        // BUG FIX N°5: Notify ALL apprenants about the new formation
        List<Utilisateur> allApprenants = utilisateurRepository.findByRoleNom("APPRENANT");
        allApprenants.forEach(apprenant ->
            notificationService.createNotification(
                apprenant,
                "Nouvelle formation disponible : « " + saved.getTitre() + " »"
            )
        );

        return map(saved);
    }

    // =========================================================
    // ADMIN - UPDATE
    // =========================================================
    @PreAuthorize("hasRole('ADMIN')")
    public FormationDTO update(Long id, FormationDTO dto) {

        Formation f = formationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Formation introuvable"));

        Utilisateur formateur = utilisateurRepository.findById(dto.getFormateurId())
                .orElseThrow(() -> new RuntimeException("Formateur introuvable"));

        Domaine domaine = domaineRepository.findById(dto.getDomaineId())
                .orElseThrow(() -> new RuntimeException("Domaine introuvable"));

        f.setTitre(dto.getTitre());
        f.setDescription(dto.getDescription());
        f.setFormateur(formateur);
        f.setDomaine(domaine);

        return map(formationRepository.save(f));
    }

    // =========================================================
    // ADMIN - DELETE
    // Relations:
    //   Formation → Module   = AGGRÉGATION : modules restent en base
    //   Formation → Test     = COMPOSITION : test supprimé automatiquement
    //   Formation → Session  = COMPOSITION : sessions supprimées automatiquement
    // =========================================================
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void delete(Long id) {

        Formation formation = formationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Formation introuvable"));

        // COMPOSITION: Supprimer le test lié (si existe)
        testRepository.findByFormationId(id).ifPresent(testRepository::delete);

        // COMPOSITION: Supprimer toutes les sessions liées
        List<SessionEnLigne> sessions = sessionRepository.findByFormation(formation);
        sessionRepository.deleteAll(sessions);

        // AGGRÉGATION: Détacher les modules (nullifier la référence formation)
        // pour qu'ils restent en base sans référence à la formation supprimée
        if (formation.getModules() != null) {
            formation.getModules().forEach(m -> m.setFormation(null));
        }

        // Supprimer la formation
        formationRepository.delete(formation);
    }

    // =========================================================
    // ADMIN - GET ALL (simple, pour la table)
    // =========================================================
    @PreAuthorize("hasRole('ADMIN')")
    public List<FormationDTO> getAllAdmin() {
        return formationRepository.findAll().stream().map(this::map).toList();
    }

    // =========================================================
    // ADMIN - GET ALL FULL (BUG FIX N°2: avec modules + docs + vidéos)
    // =========================================================
    @PreAuthorize("hasRole('ADMIN')")
    public List<FormationFullDTO> getAllAdminFull() {
        return formationRepository.findAll().stream().map(this::mapFull).toList();
    }

    // =========================================================
    // PUBLIC (VISITEUR)
    // =========================================================
    public List<FormationDTO> getPublic() {
        return formationRepository.findAll().stream().map(this::map).toList();
    }

    // =========================================================
    // FORMATEUR (SES PROPRES FORMATIONS AVEC CONTENU COMPLET)
    // =========================================================
    @PreAuthorize("hasRole('FORMATEUR')")
    public List<FormationFullDTO> getMyFormations(String email) {

        Utilisateur f = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Formateur introuvable"));

        return formationRepository.findByFormateur(f)
                .stream()
                .map(this::mapFull)
                .toList();
    }

    // =========================================================
    // APPRENANT (UNIQUEMENT SES FORMATIONS INSCRITES AVEC CONTENU)
    // =========================================================
    @PreAuthorize("hasRole('APPRENANT')")
    public List<FormationFullDTO> getMyInscribedFormations(String email) {

        Utilisateur a = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Apprenant introuvable"));

        return inscriptionRepository.findByApprenant(a)
                .stream()
                .filter(insc -> insc.isValide()) // FIX BUG #3
                .map(insc -> mapFull(insc.getFormation()))
                .toList();
    }

    // =========================================================
    // PAR DOMAINE (PUBLIC)
    // =========================================================
    public List<FormationDTO> getByDomaine(Long domaineId) {

        Domaine d = domaineRepository.findById(domaineId)
                .orElseThrow(() -> new RuntimeException("Domaine introuvable"));

        return formationRepository.findByDomaine(d)
                .stream()
                .map(this::map)
                .toList();
    }

    // =========================================================
    // UTILITAIRE
    // =========================================================
    public Formation getFormationOrThrow(Long id) {
        return formationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Formation introuvable"));
    }
}
