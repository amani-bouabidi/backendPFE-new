package com.ira.formation.services;

import com.ira.formation.dto.DocumentDTO;
import com.ira.formation.dto.ModuleDTO;
import com.ira.formation.dto.VideoDTO;
import com.ira.formation.entities.Formation;
import com.ira.formation.entities.Module;
import com.ira.formation.repositories.FormationRepository;
import com.ira.formation.repositories.ModuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ModuleService {

    private final ModuleRepository moduleRepository;
    private final FormationRepository formationRepository;

    // =================== MAPPER (BUG FIX: include documents + videos) ===================
    private ModuleDTO map(Module m) {
        return ModuleDTO.builder()
                .id(m.getId())
                .titre(m.getTitre())
                .description(m.getDescription())
                .formationId(m.getFormation() != null ? m.getFormation().getId() : null)
                // FIX: populate documents
                .documents(
                    m.getDocuments() == null ? Collections.emptyList() :
                        m.getDocuments().stream().map(d -> DocumentDTO.builder()
                            .id(d.getId())
                            .nom(d.getNom())
                            .filePath(d.getFilePath())
                            .build()
                        ).toList()
                )
                // FIX: populate videos
                .videos(
                    m.getVideos() == null ? Collections.emptyList() :
                        m.getVideos().stream().map(v -> VideoDTO.builder()
                            .id(v.getId())
                            .titre(v.getTitre())
                            .filePath(v.getFilePath())
                            .build()
                        ).toList()
                )
                .build();
    }

    // =================== CREATE ===================
    @PreAuthorize("hasRole('ADMIN')")
    public ModuleDTO create(ModuleDTO dto) {

        Formation formation = formationRepository.findById(dto.getFormationId())
                .orElseThrow(() -> new RuntimeException("Formation introuvable"));

        Module m = Module.builder()
                .titre(dto.getTitre())
                .description(dto.getDescription())
                .formation(formation)
                .build();

        return map(moduleRepository.save(m));
    }

    // =================== UPDATE ===================
    @PreAuthorize("hasRole('ADMIN')")
    public ModuleDTO update(Long id, ModuleDTO dto) {

        Module m = moduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Module introuvable"));

        m.setTitre(dto.getTitre());
        m.setDescription(dto.getDescription());

        return map(moduleRepository.save(m));
    }

    // =================== DELETE (Composition: deletes documents + videos via CascadeType.ALL) ===================
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(Long id) {
        moduleRepository.deleteById(id);
    }

    // =================== ADMIN LIST (now includes docs + videos) ===================
    @PreAuthorize("hasRole('ADMIN')")
    public List<ModuleDTO> getAll() {
        return moduleRepository.findAll()
                .stream().map(this::map).toList();
    }
}
