package com.ira.formation.services;

import com.ira.formation.dto.ModuleDTO;
import com.ira.formation.entities.Formation;
import com.ira.formation.entities.Module;
import com.ira.formation.repositories.FormationRepository;
import com.ira.formation.repositories.ModuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ModuleService {

    private final ModuleRepository moduleRepository;
    private final FormationRepository formationRepository;

    // =================== MAPPER ===================
    private ModuleDTO map(Module m) {
        return ModuleDTO.builder()
                .id(m.getId())
                .titre(m.getTitre())
                .description(m.getDescription())
                .formationId(m.getFormation().getId())
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

    // =================== DELETE ===================
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(Long id) {
        moduleRepository.deleteById(id);
    }

    // =================== ADMIN LIST ===================
    @PreAuthorize("hasRole('ADMIN')")
    public List<ModuleDTO> getAll() {
        return moduleRepository.findAll()
                .stream().map(this::map).toList();
    }
}