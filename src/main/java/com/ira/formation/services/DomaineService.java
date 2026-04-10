package com.ira.formation.services;

import com.ira.formation.dto.*;
import com.ira.formation.entities.Domaine;
import com.ira.formation.entities.Formation;
import com.ira.formation.repositories.DomaineRepository;
import com.ira.formation.repositories.FormationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DomaineService {

    private final DomaineRepository domaineRepository;
    private final FormationRepository formationRepository;

    // ✅ CREATE
    @PreAuthorize("hasRole('ADMIN')")
    public DomaineDTO createDomaine(DomaineDTO dto){

        if(domaineRepository.existsByNom(dto.getNom())){
            throw new RuntimeException("Ce domaine existe déjà");
        }

        Domaine domaine = new Domaine();
        domaine.setNom(dto.getNom());

        Domaine saved = domaineRepository.save(domaine);

        return DomaineDTO.builder()
                .id(saved.getId())
                .nom(saved.getNom())
                .build();
    }

    // ✅ GET ALL (clean)
    public List<DomaineDTO> getAllDomaines(){

        return domaineRepository.findAll()
                .stream()
                .map(d -> DomaineDTO.builder()
                        .id(d.getId())
                        .nom(d.getNom())
                        .build())
                .toList();
    }

    // ✅ DELETE
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteDomaine(Long id){

        Domaine domaine = domaineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Domaine non trouvé"));

        // نفصل relations قبل الحذف
        if(domaine.getFormations() != null){
            for(Formation f : domaine.getFormations()){
                f.setDomaine(null);
                formationRepository.save(f);
            }
        }

        domaineRepository.delete(domaine);
    }

    // ✅ DOMAINES + FORMATIONS
    public List<DomaineFormationsDTO> getDomainesWithFormations(){

        return domaineRepository.findAll()
                .stream()
                .map(domaine -> {

                    List<FormationDTO> formations = domaine.getFormations()
                            .stream()
                            .map(f -> FormationDTO.builder()
                                    .id(f.getId())
                                    .titre(f.getTitre())
                                    .description(f.getDescription())
                                    .formateurId(f.getFormateur() != null ? f.getFormateur().getId() : null)
                                    .formateurNom(f.getFormateur() != null ? f.getFormateur().getNom() : null)
                                    .domaineId(domaine.getId())
                                    .domaineNom(domaine.getNom())
                                    .build())
                            .toList();

                    return DomaineFormationsDTO.builder()
                            .domaineId(domaine.getId())
                            .nomDomaine(domaine.getNom())
                            .formations(formations)
                            .build();
                })
                .toList();
    }

    // ✅ STATS
    public List<DomaineStatsDTO> getDomainesStats(){

        return domaineRepository.findAll()
                .stream()
                .map(domaine -> DomaineStatsDTO.builder()
                        .domaineId(domaine.getId())
                        .nomDomaine(domaine.getNom())
                        .nombreFormations(
                                domaine.getFormations() != null ?
                                        (long) domaine.getFormations().size() : 0
                        )
                        .build()
                )
                .toList();
    }
}