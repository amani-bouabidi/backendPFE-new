package com.ira.formation.services;

import com.ira.formation.dto.*;
import com.ira.formation.entities.Domaine;
import com.ira.formation.repositories.DomaineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DomaineService {

    private final DomaineRepository domaineRepository;

    // ================= CREATE =================
    public DomaineDTO create(DomaineDTO dto) {

        if (domaineRepository.existsByNom(dto.getNom())) {
            throw new RuntimeException("Domaine déjà existant");
        }

        Domaine d = Domaine.builder()
                .nom(dto.getNom())
                .build();

        return mapToDTO(domaineRepository.save(d));
    }

    // ================= ADMIN LIST =================
    public List<DomaineDTO> getAll() {
        return domaineRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    // ================= PUBLIC LIST (simple) =================
    public List<DomaineDTO> getPublic() {
        return domaineRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    // ================= DELETE =================
    public void delete(Long id) {

        Domaine d = domaineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Domaine non trouvé"));

        domaineRepository.delete(d);
    }

    // ================= UPDATE =================
    public DomaineDTO update(Long id, DomaineDTO dto) {

        Domaine d = domaineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Domaine non trouvé"));

        if (!d.getNom().equals(dto.getNom()) &&
                domaineRepository.existsByNom(dto.getNom())) {
            throw new RuntimeException("Nom déjà utilisé");
        }

        d.setNom(dto.getNom());

        return mapToDTO(domaineRepository.save(d));
    }

    // ================= CATALOGUE (FIXED) =================
    public List<DomainePublicDTO> getCatalogue() {

        return domaineRepository.findAll()
                .stream()
                .map(d -> DomainePublicDTO.builder()
                        .id(d.getId())
                        .nom(d.getNom())
                        .formations(
                                d.getFormations() == null ? List.of() :
                                d.getFormations().stream()
                                        .map(f -> FormationPublicDTO.builder()
                                                .id(f.getId())
                                                .titre(f.getTitre())
                                                .description(f.getDescription())
                                                .build()
                                        )
                                        .toList()
                        )
                        .build()
                )
                .toList();
    }

    // ================= MAPPER =================
    private DomaineDTO mapToDTO(Domaine d) {
        return DomaineDTO.builder()
                .id(d.getId())
                .nom(d.getNom())
                .build();
    }
}