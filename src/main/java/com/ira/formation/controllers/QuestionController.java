package com.ira.formation.controllers;

import com.ira.formation.dto.ApiResponse;
import com.ira.formation.dto.QuestionRequestDTO;
import com.ira.formation.dto.QuestionResponseDTO; // ✅ مهم
import com.ira.formation.services.QuestionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.security.Principal;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('FORMATEUR')")
public class QuestionController {

    private final QuestionService questionService;

    // ✅ ADD QUESTION
    @PostMapping("/test/{testId}")
    public ApiResponse<QuestionResponseDTO> addQuestion(@PathVariable Long testId,
                                                        @RequestBody @Valid QuestionRequestDTO request,
                                                        Principal principal){

        QuestionResponseDTO q = questionService.addQuestion(
                testId,
                request.getTexte(),
                principal.getName()
        );

        return ApiResponse.success(q, "Question ajoutée avec succès");
    }

    // ✅ DELETE QUESTION
    @DeleteMapping("/{id}")
    public ApiResponse<Object> deleteQuestion(@PathVariable Long id,
                                              Principal principal){

        questionService.deleteQuestion(id, principal.getName());

        return ApiResponse.success(null, "Question supprimée avec succès");
    }
}