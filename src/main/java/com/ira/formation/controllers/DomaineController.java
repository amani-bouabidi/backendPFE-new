package com.ira.formation.controllers;

import com.ira.formation.dto.DomaineDTO;
import com.ira.formation.dto.DomainePublicDTO;
import com.ira.formation.services.DomaineService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/domaines")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class DomaineController {

    private final DomaineService domaineService;

    // =================== ADMIN CRUD ===================
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public DomaineDTO create(@RequestBody DomaineDTO dto) {
        return domaineService.create(dto);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public List<DomaineDTO> getAll() {
        return domaineService.getAll();
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public DomaineDTO update(@PathVariable Long id,
                             @RequestBody DomaineDTO dto) {
        return domaineService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        domaineService.delete(id);
    }

    // =================== PUBLIC HOME ===================
    @GetMapping("/public")
    public List<DomaineDTO> getPublic() {
        return domaineService.getPublic();
    }
    
    
    @GetMapping("/catalogue")
    public List<DomainePublicDTO> getCatalogue() {
        return domaineService.getCatalogue();
    }
}