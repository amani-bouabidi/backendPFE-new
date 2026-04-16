package com.ira.formation.controllers;

import com.ira.formation.dto.UtilisateurCreationDTO;
import com.ira.formation.dto.UtilisateurResponseDTO;
import com.ira.formation.dto.UtilisateurUpdateDTO;
import com.ira.formation.services.UtilisateurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    // =====================================================
    // CREATE FORMATEUR
    // =====================================================
    @PostMapping("/formateurs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UtilisateurResponseDTO> creerFormateur(
            @Valid @RequestBody UtilisateurCreationDTO dto) {

        return ResponseEntity.status(201)
                .body(utilisateurService.creerFormateur(dto));
    }

    // =====================================================
    // UPDATE FORMATEUR
    // =====================================================
    @PutMapping("/formateurs/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UtilisateurResponseDTO> modifierFormateur(
            @PathVariable Long id,
            @Valid @RequestBody UtilisateurUpdateDTO dto) {

        return ResponseEntity.ok(
                utilisateurService.modifierFormateur(id, dto)
        );
    }

    // =====================================================
    // DELETE FORMATEUR
    // =====================================================
    @DeleteMapping("/formateurs/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> supprimerFormateur(@PathVariable Long id) {
        utilisateurService.supprimerFormateur(id);
        return ResponseEntity.noContent().build();
    }

    // =====================================================
    // DELETE APPRENANT
    // =====================================================
    @DeleteMapping("/apprenants/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> supprimerApprenant(@PathVariable Long id) {
        utilisateurService.supprimerApprenant(id);
        return ResponseEntity.noContent().build();
    }

    // =====================================================
    // LIST FORMATEURS
    // =====================================================
    @GetMapping("/formateurs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UtilisateurResponseDTO>> listerFormateurs() {
        return ResponseEntity.ok(utilisateurService.getAllFormateurs());
    }

    // =====================================================
    // LIST APPRENANTS
    // =====================================================
    @GetMapping("/apprenants")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UtilisateurResponseDTO>> listerApprenants() {
        return ResponseEntity.ok(utilisateurService.getAllApprenants());
    }
}