package com.ira.formation.controllers;

import com.ira.formation.dto.ApiResponse;
import com.ira.formation.dto.ChoixRequestDTO;
import com.ira.formation.dto.ChoixResponseDTO;
import com.ira.formation.services.ChoixService;
import lombok.RequiredArgsConstructor;

import java.security.Principal;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/choix")
@RequiredArgsConstructor
@PreAuthorize("hasRole('FORMATEUR')")
public class ChoixController {

    private final ChoixService choixService;

    @PostMapping("/question/{questionId}")
    public ApiResponse<ChoixResponseDTO> addChoix(@PathVariable Long questionId,
                                                 @RequestBody @Valid ChoixRequestDTO request,
                                                 Principal principal){

        ChoixResponseDTO c = choixService.addChoix(
                questionId,
                request.getTexte(),
                request.isCorrect(),
                principal.getName()
        );

        return ApiResponse.success(c, "Choix ajouté avec succès");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Object> deleteChoix(@PathVariable Long id,
                                           Principal principal){

        choixService.deleteChoix(id, principal.getName());

        return ApiResponse.success(null, "Choix supprimé avec succès");
    }
}