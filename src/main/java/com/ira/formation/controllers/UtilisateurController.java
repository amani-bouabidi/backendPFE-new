package com.ira.formation.controllers;

import com.ira.formation.dto.UtilisateurCreationDTO;
import com.ira.formation.dto.UtilisateurResponseDTO;
import com.ira.formation.dto.UtilisateurUpdateDTO;
import com.ira.formation.services.UtilisateurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    // =====================================================================
    //                  Création de formateur (admin only)
    // =====================================================================
    @PostMapping("/formateurs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UtilisateurResponseDTO> creerFormateur(
            @Valid @RequestBody UtilisateurCreationDTO dto) {
        return ResponseEntity.status(201).body(utilisateurService.creerFormateur(dto));
    }

    // =====================================================================
    //                  Mise à jour formateur (admin only)
    // =====================================================================
    @PutMapping("/formateurs/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UtilisateurResponseDTO> modifierFormateur(
            @PathVariable Long id,
            @Valid @RequestBody UtilisateurUpdateDTO dto) {

        UtilisateurResponseDTO updated = utilisateurService.modifierFormateur(id, dto);
        return ResponseEntity.ok(updated);
    }

    // =====================================================================
    //                  Suppression formateur (admin only)
    // =====================================================================
    @DeleteMapping("/formateurs/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> supprimerFormateur(@PathVariable Long id) {
        utilisateurService.supprimerFormateur(id);
        return ResponseEntity.noContent().build();  // 204 No Content
    }

    // =====================================================================
    //                  Suppression apprenant (admin only)
    // =====================================================================
    @DeleteMapping("/apprenants/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> supprimerApprenant(@PathVariable Long id) {
        utilisateurService.supprimerApprenant(id);
        return ResponseEntity.noContent().build();
    }

    // =====================================================================
    //                  Lister (optionnel pour debug/admin dashboard)
    // =====================================================================
    @GetMapping("/formateurs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UtilisateurResponseDTO>> listerFormateurs() {
        return ResponseEntity.ok(utilisateurService.getAllFormateurs());
    }

    @GetMapping("/apprenants")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UtilisateurResponseDTO>> listerApprenants() {
        return ResponseEntity.ok(utilisateurService.getAllApprenants());
    }
}