package com.ira.formation.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChoixResponseDTO {
    private Long id;
    private String texte;
    private boolean correct;
    private Long questionId;
}