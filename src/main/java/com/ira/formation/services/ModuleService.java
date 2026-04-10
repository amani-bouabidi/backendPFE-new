package com.ira.formation.services;

import com.ira.formation.dto.DocumentDTO;
import com.ira.formation.dto.VideoDTO;
import com.ira.formation.dto.ModuleContentDTO;
import com.ira.formation.dto.ModuleDTO;
import com.ira.formation.entities.Formation;
import com.ira.formation.entities.Inscription;
import com.ira.formation.entities.Module;
import com.ira.formation.entities.Utilisateur;
import com.ira.formation.repositories.FormationRepository;
import com.ira.formation.repositories.InscriptionRepository;
import com.ira.formation.repositories.ModuleRepository;
import com.ira.formation.repositories.UtilisateurRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ModuleService {

    private final ModuleRepository moduleRepository;
    private final FormationRepository formationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final InscriptionRepository inscriptionRepository;

    private ModuleDTO mapToDTO(Module module) {
        return ModuleDTO.builder()
                .id(module.getId())
                .titre(module.getTitre())
                .description(module.getDescription())
                .formationId(module.getFormation().getId())
                .build();
    }

    private Module mapToEntity(ModuleDTO dto) {

        Formation formation = formationRepository.findById(dto.getFormationId())
                .orElseThrow(() -> new RuntimeException("Formation non trouvée"));

        return Module.builder()
                .titre(dto.getTitre())
                .description(dto.getDescription())
                .formation(formation)
                .build();
    }

    // CREATE
    @PreAuthorize("hasRole('ADMIN')")
    public ModuleDTO creerModule(ModuleDTO dto) {

        Module module = mapToEntity(dto);

        Module saved = moduleRepository.save(module);

        return mapToDTO(saved);
    }

    // READ
    @PreAuthorize("hasAnyRole('ADMIN','FORMATEUR','APPRENANT')")
    public List<ModuleDTO> getModules(Long formationId, String email) {

        Formation formation = formationRepository.findById(formationId)
                .orElseThrow(() -> new RuntimeException("Formation non trouvée"));

        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // ✅ إذا apprenant → لازم يكون نجح في test
        if("APPRENANT".equals(user.getRole().getNom())){

            Inscription inscription = inscriptionRepository
                    .findByApprenantAndFormation(user, formation)
                    .orElseThrow(() -> new RuntimeException("Vous devez passer le test d'abord"));

            if(!inscription.isValide()){
                throw new RuntimeException("Vous devez réussir le test pour accéder au contenu");
            }
        }

        return moduleRepository.findByFormation(formation)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    // UPDATE
    @PreAuthorize("hasRole('ADMIN')")
    public ModuleDTO modifierModule(Long id, ModuleDTO dto) {

        Module existing = moduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Module non trouvé"));

        existing.setTitre(dto.getTitre());
        existing.setDescription(dto.getDescription());

        Module updated = moduleRepository.save(existing);

        return mapToDTO(updated);
    }

    // DELETE
    @PreAuthorize("hasRole('ADMIN')")
    public void supprimerModule(Long id) {

        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Module non trouvé"));

        moduleRepository.delete(module);
    }
    
    
    
    
    
    public ModuleContentDTO getModuleContent(Long moduleId, String email) {

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module non trouvé"));

        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        String role = user.getRole().getNom();

        if ("APPRENANT".equals(role)) {
            // Vérifie inscription et réussite test
            Formation formation = module.getFormation();
            Inscription inscription = inscriptionRepository
                    .findByApprenantAndFormation(user, formation)
                    .orElseThrow(() -> new RuntimeException("Vous devez passer le test d'abord"));

            if (!inscription.isValide()) {
                throw new RuntimeException("Vous devez réussir le test pour accéder au contenu");
            }
        } else if ("FORMATEUR".equals(role)) {
            // Vérifie que c'est le formateur de la formation
            if (!module.getFormation().getFormateur().getEmail().equals(email)) {
                throw new RuntimeException("Accès refusé : vous n'êtes pas le formateur de cette formation");
            }
        }
        // Admin → accès libre

        // Ensuite renvoie les contenus
        List<DocumentDTO> documents = module.getDocuments()
                .stream()
                .map(doc -> DocumentDTO.builder()
                        .id(doc.getId())
                        .nom(doc.getNom())
                        .filePath(doc.getFilePath())
                        .moduleId(moduleId)
                        .build())
                .toList();

        List<VideoDTO> videos = module.getVideos()
                .stream()
                .map(video -> VideoDTO.builder()
                        .id(video.getId())
                        .titre(video.getTitre())
                        .filePath(video.getFilePath())
                        .moduleId(moduleId)
                        .build())
                .toList();

        return ModuleContentDTO.builder()
                .moduleTitre(module.getTitre())
                .documents(documents)
                .videos(videos)
                .build();
    }
    
    }