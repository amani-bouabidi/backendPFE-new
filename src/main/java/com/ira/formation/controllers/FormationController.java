package com.ira.formation.controllers;
import com.ira.formation.dto.*;
import com.ira.formation.entities.Formation;
import com.ira.formation.entities.Utilisateur;
import com.ira.formation.services.FormationService;
import com.ira.formation.repositories.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/formations")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class FormationController {

    private final FormationService formationService;
    private final UtilisateurRepository utilisateurRepository;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FormationDTO> creerFormation(@RequestBody FormationDTO dto) {
        return ResponseEntity.status(201).body(formationService.creerFormation(dto));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<FormationDTO>> getAllFormations(Pageable pageable) {
        return ResponseEntity.ok(formationService.getAllFormations(pageable));
    }
    
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<FormationDTO>> getAllFormationsSimple() {
        return ResponseEntity.ok(formationService.getAllFormations());
    }

    @GetMapping("/formateur/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('FORMATEUR')")
    public ResponseEntity<List<FormationDTO>> getFormationsParFormateur(@PathVariable Long id) {
        Utilisateur formateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Formateur non trouvé"));
        return ResponseEntity.ok(formationService.getFormationsParFormateur(formateur));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FormationDTO> modifierFormation(
            @PathVariable Long id,
            @RequestBody FormationDTO dto) {

        return ResponseEntity.ok(formationService.modifierFormation(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> supprimerFormation(@PathVariable Long id) {
        formationService.supprimerFormation(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{id}/modules")
    public FormationModulesDTO getFormationModules(@PathVariable Long id,
                                                   Principal principal) {

        return formationService.getFormationWithModules(
                id,
                principal.getName()
        );
    }
    @GetMapping("/domaine/{id}")
    public ResponseEntity<List<FormationDTO>> getFormationsByDomaine(@PathVariable Long id){

        return ResponseEntity.ok(
                formationService.getFormationsByDomaine(id)
        );
    }
}