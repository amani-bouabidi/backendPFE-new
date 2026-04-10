package com.ira.formation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class QuestionRequestDTO {

    @NotBlank(message = "texte obligatoire")
    private String texte;
}