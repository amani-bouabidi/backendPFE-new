package com.ira.formation.services;

import com.ira.formation.dto.*;
import com.ira.formation.entities.*;
import com.ira.formation.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FormationService {

    private final FormationRepository formationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final DomaineRepository domaineRepository;
    private final InscriptionRepository inscriptionRepository;

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
    // MAPPER FULL (CONTENU)
    // =========================================================
    private FormationFullDTO mapFull(Formation f) {

        return FormationFullDTO.builder()
                .id(f.getId())
                .titre(f.getTitre())
                .description(f.getDescription())

                .modules(
                        f.getModules() == null ? Collections.emptyList() :
                                f.getModules().stream().map(m -> ModuleDTO.builder()
                                        .id(m.getId())
                                        .titre(m.getTitre())

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
    // ADMIN
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

        return map(formationRepository.save(f));
    }

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

    @PreAuthorize("hasRole('ADMIN')")
    public void delete(Long id) {
        formationRepository.deleteById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<FormationDTO> getAllAdmin() {
        return formationRepository.findAll().stream().map(this::map).toList();
    }

    // =========================================================
    // PUBLIC (VISITOR)
    // =========================================================
    public List<FormationDTO> getPublic() {
        return formationRepository.findAll().stream().map(this::map).toList();
    }

    // =========================================================
    // FORMATEUR (HIS OWN FULL CONTENT)
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
    // APPRENANT (ONLY INSCRIBED FULL CONTENT)
    // =========================================================
    @PreAuthorize("hasRole('APPRENANT')")
    public List<FormationFullDTO> getMyInscribedFormations(String email) {

        Utilisateur a = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Apprenant introuvable"));

        return inscriptionRepository.findByApprenant(a)
                .stream()
                .map(insc -> mapFull(insc.getFormation()))
                .toList();
    }

    // =========================================================
    // BY DOMAINE (PUBLIC)
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
    // SECURITY HELP
    // =========================================================
    public Formation getFormationOrThrow(Long id) {
        return formationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Formation introuvable"));
    }
}