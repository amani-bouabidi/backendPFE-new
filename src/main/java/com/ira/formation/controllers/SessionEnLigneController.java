package com.ira.formation.controllers;

import com.ira.formation.dto.ApiResponse;
import com.ira.formation.dto.SessionEnLigneDTO;
import com.ira.formation.services.SessionEnLigneService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class SessionEnLigneController {

    private final SessionEnLigneService sessionService;

    // =================== CREATE (FORMATEUR) ===================
    @PostMapping("/creer")
    @PreAuthorize("hasRole('FORMATEUR')")
    public ApiResponse<SessionEnLigneDTO> creerSession(
            @RequestParam Long formationId,
            @RequestParam String titre,
            Principal principal) {

        return ApiResponse.success(
                sessionService.creerSession(formationId, titre, principal.getName()),
                "Session créée avec succès"
        );
    }

    // =================== FORMATEUR LIST ===================
    @GetMapping("/formateur")
    @PreAuthorize("hasRole('FORMATEUR')")
    public ApiResponse<List<SessionEnLigneDTO>> listerSessionsParFormateur(Principal principal) {

        return ApiResponse.success(
                sessionService.listerSessionsParFormateur(principal.getName()),
                "Liste des sessions du formateur"
        );
    }

    // =================== BUG FIX N°3: APPRENANT - toutes ses sessions ===================
    @GetMapping("/apprenant")
    @PreAuthorize("hasRole('APPRENANT')")
    public ApiResponse<List<SessionEnLigneDTO>> getAllSessionsForApprenant(Principal principal) {

        return ApiResponse.success(
                sessionService.getAllSessionsForApprenant(principal.getName()),
                "Sessions de l'apprenant"
        );
    }

    // =================== BUG FIX N°3: APPRENANT - sessions d'une formation spécifique ===================
    @GetMapping("/apprenant/formation/{formationId}")
    @PreAuthorize("hasRole('APPRENANT')")
    public ApiResponse<List<SessionEnLigneDTO>> getSessionsByFormationForApprenant(
            @PathVariable Long formationId,
            Principal principal) {

        return ApiResponse.success(
                sessionService.getSessionsByFormationForApprenant(formationId, principal.getName()),
                "Sessions de la formation"
        );
    }

    // =================== GET SECURE (FORMATEUR ou APPRENANT) ===================
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('FORMATEUR','APPRENANT')")
    public ApiResponse<SessionEnLigneDTO> getSessionById(
            @PathVariable Long id,
            Principal principal) {

        return ApiResponse.success(
                sessionService.getSessionSecure(id, principal.getName()),
                "Détails de la session"
        );
    }

    // =================== UPDATE (FORMATEUR) ===================
    @PutMapping("/mettre-a-jour/{id}")
    @PreAuthorize("hasRole('FORMATEUR')")
    public ApiResponse<SessionEnLigneDTO> mettreAJourTitre(
            @PathVariable Long id,
            @RequestParam String nouveauTitre,
            Principal principal) {

        return ApiResponse.success(
                sessionService.mettreAJourTitre(id, nouveauTitre, principal.getName()),
                "Session mise à jour avec succès"
        );
    }

    // =================== DELETE (FORMATEUR) ===================
    @DeleteMapping("/supprimer/{id}")
    @PreAuthorize("hasRole('FORMATEUR')")
    public ApiResponse<Void> supprimerSession(
            @PathVariable Long id,
            Principal principal) {

        sessionService.supprimerSession(id, principal.getName());
        return ApiResponse.success(null, "Session supprimée avec succès");
    }

    // =================== TERMINER (FORMATEUR) ===================
    @PutMapping("/terminer/{id}")
    @PreAuthorize("hasRole('FORMATEUR')")
    public ApiResponse<SessionEnLigneDTO> terminerSession(
            @PathVariable Long id,
            Principal principal) {

        return ApiResponse.success(
                sessionService.terminerSession(id, principal.getName()),
                "Session terminée avec succès"
        );
    }
}
