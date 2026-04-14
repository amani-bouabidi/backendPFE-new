package com.ira.formation.controllers;

import com.ira.formation.dto.ProgressDTO;
import com.ira.formation.dto.ProgressForFormateurDTO;
import com.ira.formation.entities.ModuleCompletion;
import com.ira.formation.services.ProgressService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    // =================== COMPLETE MODULE ===================
    @PostMapping("/complete")
    public ProgressDTO completeModule(
            @RequestParam Long formationId,
            @RequestParam Long moduleId,
            Authentication auth
    ) {
        return progressService.completeModule(
                auth.getName(),
                formationId,
                moduleId
        );
    }

    // =================== GET MY PROGRESS ===================
    @GetMapping("/my")
    public ProgressDTO getMyProgress(
            @RequestParam Long formationId,
            Authentication auth
    ) {
        return progressService.getMyProgress(
                auth.getName(),
                formationId
        );
    }

    // =================== FORMATEUR VIEW ===================
    @GetMapping("/formation/{formationId}")
    @PreAuthorize("hasRole('FORMATEUR')")
    public List<ProgressForFormateurDTO> getFormationProgress(
            @PathVariable Long formationId,
            Authentication auth
    ) {
        return progressService.getProgressByFormation(auth.getName(), formationId);
    }
    
    @GetMapping("/resume")
    @PreAuthorize("hasRole('APPRENANT')")
    public Long resume(
            @RequestParam Long formationId,
            Authentication auth
    ) {
        return progressService.getLastModule(auth.getName(), formationId);
    }
}