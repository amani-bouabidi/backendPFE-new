package com.ira.formation.controllers;

import com.ira.formation.dto.ApiResponse;
import com.ira.formation.dto.TestApprenantDTO;
import com.ira.formation.dto.TestRequestDTO;
import com.ira.formation.dto.TestResponseDTO;
import com.ira.formation.entities.Test;
import com.ira.formation.services.TestService;
import lombok.RequiredArgsConstructor;

import java.security.Principal;
import java.util.Map;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class TestController {

    private final TestService testService;

    // =================== CREATE (FORMATEUR seulement) ===================
    @PostMapping
    @PreAuthorize("hasRole('FORMATEUR')")
    public ApiResponse<TestResponseDTO> createTest(@RequestBody @Valid TestRequestDTO request,
                                                   Principal principal){
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
    public ApiResponse<TestResponseDTO> getTestByFormation(@PathVariable Long formationId,
                                                           Principal principal){
        TestResponseDTO test = testService.getTestByFormation(
                formationId,
                principal.getName()
        );
        return ApiResponse.success(test, "Test récupéré avec succès");
    }

    // =================== UPDATE ===================
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('FORMATEUR')")
    public ApiResponse<TestResponseDTO> updateTest(@PathVariable Long id,
                                                   @RequestBody @Valid TestRequestDTO request,
                                                   Principal principal){
        TestResponseDTO test = testService.updateTest(
                id,
                request.getTitre(),
                principal.getName()
        );
        return ApiResponse.success(test, "Test mis à jour avec succès");
    }

    // =================== DELETE ===================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('FORMATEUR')")
    public ApiResponse<Object> deleteTest(@PathVariable Long id,
                                          Principal principal){
        testService.deleteTest(id, principal.getName());
        return ApiResponse.success(null, "Test supprimé avec succès");
    }

    // =================== GET (Apprenant) ===================
    @GetMapping("/formation/{formationId}/apprenant")
    @PreAuthorize("hasRole('APPRENANT')")
    public ApiResponse<TestApprenantDTO> getTestForApprenant(@PathVariable Long formationId,
                                                             Principal principal){
        TestApprenantDTO testDTO = testService.getTestByFormationForApprenant(
                formationId, principal.getName()
        );
        return ApiResponse.success(testDTO, "Test récupéré avec succès");
    }

    // =================== PASSER TEST (APPRENANT) ===================
    @PostMapping("/pass/{formationId}")
    @PreAuthorize("hasRole('APPRENANT')")
    public Map<String, Object> passerTest(@PathVariable Long formationId,
                                          @RequestBody Map<Long, Long> reponses,
                                          Principal principal){
        return testService.passerTest(
                formationId,
                reponses,
                principal.getName()
        );
    }
}