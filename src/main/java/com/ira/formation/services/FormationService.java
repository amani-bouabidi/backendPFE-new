package com.ira.formation.services;

import com.ira.formation.dto.FormationDTO;
import com.ira.formation.dto.FormationModulesDTO;
import com.ira.formation.dto.ModuleDTO;
import com.ira.formation.entities.Domaine;
import com.ira.formation.entities.Formation;
import com.ira.formation.entities.Utilisateur;
import com.ira.formation.repositories.DomaineRepository;
import com.ira.formation.repositories.FormationRepository;
import com.ira.formation.repositories.InscriptionRepository;
import com.ira.formation.repositories.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FormationService {

    private final FormationRepository formationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final DomaineRepository domaineRepository;
    private final NotificationService notificationService;
    private final InscriptionRepository inscriptionRepository;

    // Mapper simple entity → DTO
    private FormationDTO mapToDTO(Formation f) {
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

    // Création
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public FormationDTO creerFormation(FormationDTO dto) {

        // 🔥 نجيبو formateur
        Utilisateur formateur = utilisateurRepository.findById(dto.getFormateurId())
                .orElseThrow(() -> new RuntimeException("Formateur non trouvé"));

        // 🔥 نتحقق duplication
        if (formationRepository.existsByTitreAndFormateur(dto.getTitre(), formateur)) {
            throw new RuntimeException("Ce formateur a déjà une formation avec ce titre");
        }

        // 🔥 نجيبو domaine
        Domaine domaine = domaineRepository.findById(dto.getDomaineId())
                .orElseThrow(() -> new RuntimeException("Domaine non trouvé"));

        // 🔥 نبنيو formation
        Formation formation = Formation.builder()
                .titre(dto.getTitre())
                .description(dto.getDescription())
                .formateur(formateur)
                .domaine(domaine)
                .build();

        Formation saved = formationRepository.save(formation);

     // ✅ envoyer notification
     List<Utilisateur> apprenants = utilisateurRepository.findByRoleNom("APPRENANT");

     for(Utilisateur u : apprenants){
         notificationService.createNotification(
                 u,
                 "Nouvelle formation disponible: " + saved.getTitre()
         );
     }

     // ✅ IMPORTANT: خارج ال loop
     return mapToDTO(saved);
    }

    // Liste toutes les formations
    @PreAuthorize("hasRole('ADMIN')")
    public Page<FormationDTO> getAllFormations(Pageable pageable){
        return formationRepository.findAll(pageable)
                .map(this::mapToDTO);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    public List<FormationDTO> getAllFormations() {
        return formationRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    // Liste formations d’un formateur
    @PreAuthorize("hasRole('ADMIN') or hasRole('FORMATEUR')")
    public List<FormationDTO> getFormationsParFormateur(Utilisateur formateur) {
        return formationRepository.findByFormateur(formateur)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    // Modifier formation
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public FormationDTO modifierFormation(Long id, FormationDTO dto) {

        Formation existing = formationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Formation non trouvée"));

        Utilisateur formateur = utilisateurRepository.findById(dto.getFormateurId())
                .orElseThrow(() -> new RuntimeException("Formateur non trouvé"));

        // 🔥 Vérification duplication sauf si id courant
        boolean duplicate = formationRepository.findByFormateur(formateur).stream()
                .anyMatch(f -> f.getTitre().equalsIgnoreCase(dto.getTitre()) && !f.getId().equals(id));

        if (duplicate) {
            throw new RuntimeException("Ce formateur a déjà une formation avec ce titre");
        }

        Domaine domaine = domaineRepository.findById(dto.getDomaineId())
                .orElseThrow(() -> new RuntimeException("Domaine non trouvé"));

        existing.setTitre(dto.getTitre());
        existing.setDescription(dto.getDescription());
        existing.setFormateur(formateur);
        existing.setDomaine(domaine);

        return mapToDTO(formationRepository.save(existing));
    }

    // Supprimer formation
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void supprimerFormation(Long id) {
        Formation formation = formationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Formation non trouvée"));
        formationRepository.delete(formation);
    }

    public FormationModulesDTO getFormationWithModules(Long formationId, String email) {

        Utilisateur apprenant = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Formation formation = formationRepository.findById(formationId)
                .orElseThrow(() -> new RuntimeException("Formation non trouvée"));

        // 🔐 check inscription
        boolean inscrit = inscriptionRepository
                .existsByApprenantAndFormation(apprenant, formation);

        if(!inscrit){
            throw new RuntimeException("Accès refusé à cette formation");
        }

        List<ModuleDTO> modules = formation.getModules()
                .stream()
                .map(module -> ModuleDTO.builder()
                        .id(module.getId())
                        .titre(module.getTitre())
                        .description(module.getDescription())
                        .formationId(formationId)
                        .build())
                .toList();

        return FormationModulesDTO.builder()
                .formationId(formationId)
                .titreFormation(formation.getTitre())
                .modules(modules)
                .build();
    }

    public List<FormationDTO> getFormationsByDomaine(Long domaineId){

        Domaine domaine = domaineRepository.findById(domaineId)
                .orElseThrow(() -> new RuntimeException("Domaine non trouvé"));

        return formationRepository.findByDomaine(domaine)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }
}