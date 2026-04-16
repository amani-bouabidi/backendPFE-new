package com.ira.formation.controllers;

import com.ira.formation.dto.ModuleDTO;
import com.ira.formation.services.ModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/modules")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ModuleController {

    private final ModuleService moduleService;

    // =================== ADMIN ONLY ===================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ModuleDTO create(@RequestBody ModuleDTO dto) {
        return moduleService.create(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ModuleDTO update(@PathVariable Long id,
                            @RequestBody ModuleDTO dto) {
        return moduleService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        moduleService.delete(id);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ModuleDTO> getAll() {
        return moduleService.getAll();
    }
}