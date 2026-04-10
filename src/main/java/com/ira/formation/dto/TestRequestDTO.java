package com.ira.formation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TestRequestDTO {

    @NotNull(message = "formationId obligatoire")
    private Long formationId;

    @NotBlank(message = "titre obligatoire")
    private String titre;
}