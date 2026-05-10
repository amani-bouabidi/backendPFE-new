package com.ira.formation.controllers;

import com.ira.formation.dto.ApiResponse;
import com.ira.formation.dto.TestApprenantDTO;
import com.ira.formation.dto.TestRequestDTO;
import com.ira.formation.dto.TestResponseDTO;
import com.ira.formation.services.TestService;
import lombok.RequiredArgsConstructor;

import java.security.Principal;
import java.util.Map;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class TestController {

    private final TestService testService;

    // =================== CREATE (FORMATEUR) ===================
    @PostMapping
    @PreAuthorize("hasRole('FORMATEUR')")
    public ApiResponse<TestResponseDTO> createTest(
            @RequestBody @Valid TestRequestDTO request,
            Principal principal) {

        TestResponseDTO test = testService.createTest(
                request.getFormationId(),
                request.getTitre(),
                principal.getName()
        );
        return ApiResponse.success(test, "Test créé avec succès");
    }

    // =================== GET (FORMATEUR) ===================
    @GetMapping("/formation/{formationId}")
    @PreAuthorize("hasRole('FORMATEUR')")
    public ApiResponse<TestResponseDTO> getTestByFormation(
            @PathVariable Long formationId,
            Principal principal) {

        TestResponseDTO test = testService.getTestByFormation(
                formationId, principal.getName()
        );
        return ApiResponse.success(test, "Test récupéré avec succès");
    }

    // =================== UPDATE (FORMATEUR) ===================
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('FORMATEUR')")
    public ApiResponse<TestResponseDTO> updateTest(
            @PathVariable Long id,
            @RequestBody @Valid TestRequestDTO request,
            Principal principal) {

        TestResponseDTO test = testService.updateTest(
                id, request.getTitre(), principal.getName()
        );
        return ApiResponse.success(test, "Test mis à jour avec succès");
    }

    // =================== DELETE (FORMATEUR) ===================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('FORMATEUR')")
    public ApiResponse<Object> deleteTest(
            @PathVariable Long id,
            Principal principal) {

        testService.deleteTest(id, principal.getName());
        return ApiResponse.success(null, "Test supprimé avec succès");
    }

    // =================== GET TEST (APPRENANT) ===================
    // FIX N°1: Returns 409 CONFLICT if apprenant is already validly inscribed
    // Frontend detects 409 → redirects to formation content directly
    // Returns 404 if no test exists for this formation
    @GetMapping("/formation/{formationId}/apprenant")
    @PreAuthorize("hasRole('APPRENANT')")
    public ResponseEntity<?> getTestForApprenant(
            @PathVariable Long formationId,
            Principal principal) {

        try {
            TestApprenantDTO testDTO = testService.getTestByFormationForApprenant(
                    formationId, principal.getName()
            );
            return ResponseEntity.ok(ApiResponse.success(testDTO, "Test récupéré avec succès"));

        } catch (RuntimeException e) {
            if ("ALREADY_INSCRIBED".equals(e.getMessage())) {
                // 409 Conflict: apprenant already passed the test and is inscribed
                // Frontend should show formation content directly
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(Map.of(
                                "success", false,
                                "message", "ALREADY_INSCRIBED",
                                "detail", "Vous êtes déjà inscrit à cette formation."
                        ));
            }
            // 404: no test configured for this formation
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "success", false,
                            "message", "NO_TEST",
                            "detail", e.getMessage()
                    ));
        }
    }

    // =================== PASSER TEST (APPRENANT) ===================
    @PostMapping("/pass/{formationId}")
    @PreAuthorize("hasRole('APPRENANT')")
    public Map<String, Object> passerTest(
            @PathVariable Long formationId,
            @RequestBody Map<Long, Long> reponses,
            Principal principal) {

        return testService.passerTest(formationId, reponses, principal.getName());
    }
}
