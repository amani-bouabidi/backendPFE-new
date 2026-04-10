package com.ira.formation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChoixRequestDTO {

    @NotBlank(message = "texte obligatoire")
    private String texte;

    private boolean correct;
}