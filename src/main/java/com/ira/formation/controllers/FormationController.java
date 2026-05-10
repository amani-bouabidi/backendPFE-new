package com.ira.formation.controllers;

import com.ira.formation.dto.FormationDTO;
import com.ira.formation.dto.FormationFullDTO;
import com.ira.formation.services.FormationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/formations")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class FormationController {

    private final FormationService formationService;

    // =================== ADMIN CRUD ===================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public FormationDTO create(@RequestBody FormationDTO dto) {
        return formationService.create(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public FormationDTO update(@PathVariable Long id,
                               @RequestBody FormationDTO dto) {
        return formationService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        formationService.delete(id);
    }

    // =================== ADMIN VIEW (simple - pour la table) ===================

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public List<FormationDTO> getAll() {
        return formationService.getAllAdmin();
    }

    // =================== ADMIN VIEW FULL (BUG FIX N°2: avec modules + docs + vidéos) ===================

    @GetMapping("/admin/full")
    @PreAuthorize("hasRole('ADMIN')")
    public List<FormationFullDTO> getAllFull() {
        return formationService.getAllAdminFull();
    }

    // =================== PUBLIC VISITEUR ===================

    @GetMapping("/public")
    public List<FormationDTO> getPublic() {
        return formationService.getPublic();
    }

    // =================== FORMATEUR (CONTENU COMPLET) ===================

    @GetMapping("/formateur")
    @PreAuthorize("hasRole('FORMATEUR')")
    public List<FormationFullDTO> getMyFormations(Principal principal) {
        return formationService.getMyFormations(principal.getName());
    }

    // =================== APPRENANT (CONTENU COMPLET SI INSCRIT) ===================

    @GetMapping("/apprenant")
    @PreAuthorize("hasRole('APPRENANT')")
    public List<FormationFullDTO> getMyFormationsApprenant(Principal principal) {
        return formationService.getMyInscribedFormations(principal.getName());
    }

    // =================== FILTRE PAR DOMAINE ===================

    @GetMapping("/domaine/{id}")
    public List<FormationDTO> getByDomaine(@PathVariable Long id) {
        return formationService.getByDomaine(id);
    }
}
