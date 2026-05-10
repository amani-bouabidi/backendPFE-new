package com.ira.formation.controllers;

import com.ira.formation.dto.ProgressDTO;
import com.ira.formation.dto.ProgressForAdminDTO;
import com.ira.formation.dto.ProgressForFormateurDTO;
import com.ira.formation.services.ProgressService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ProgressController {

    private final ProgressService progressService;

    // =================== APPRENANT: MARQUER MODULE COMPLÉTÉ ===================
    @PostMapping("/complete")
    @PreAuthorize("hasRole('APPRENANT')")
    public ProgressDTO completeModule(
            @RequestParam Long formationId,
            @RequestParam Long moduleId,
            Authentication auth) {

        return progressService.completeModule(auth.getName(), formationId, moduleId);
    }

    // =================== APPRENANT: MA PROGRESSION ===================
    @GetMapping("/my")
    @PreAuthorize("hasRole('APPRENANT')")
    public ProgressDTO getMyProgress(
            @RequestParam Long formationId,
            Authentication auth) {

        return progressService.getMyProgress(auth.getName(), formationId);
    }

    // =================== APPRENANT: REPRENDRE ===================
    @GetMapping("/resume")
    @PreAuthorize("hasRole('APPRENANT')")
    public Long resume(
            @RequestParam Long formationId,
            Authentication auth) {

        return progressService.getLastModule(auth.getName(), formationId);
    }

    // =================== FORMATEUR: PROGRESSION PAR FORMATION ===================
    @GetMapping("/formation/{formationId}")
    @PreAuthorize("hasRole('FORMATEUR')")
    public List<ProgressForFormateurDTO> getFormationProgress(
            @PathVariable Long formationId,
            Authentication auth) {

        return progressService.getProgressByFormation(auth.getName(), formationId);
    }

    // =================== ADMIN: TOUTES LES PROGRESSIONS ===================
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ProgressForAdminDTO> getAllProgressions() {
        return progressService.getAllProgressForAdmin();
    }

    // =================== ADMIN: PROGRESSIONS D'UNE FORMATION ===================
    @GetMapping("/admin/formation/{formationId}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ProgressForAdminDTO> getProgressionsByFormation(
            @PathVariable Long formationId) {

        return progressService.getProgressByFormationForAdmin(formationId);
    }

    // =================== ADMIN: PROGRESSIONS D'UN APPRENANT ===================
    @GetMapping("/admin/apprenant/{apprenantId}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ProgressForAdminDTO> getProgressionsByApprenant(
            @PathVariable Long apprenantId) {

        return progressService.getProgressByApprenantForAdmin(apprenantId);
    }
}
