package com.ira.formation.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChoixApprenantDTO {
    private Long id;
    private String texte;
    private Long questionId;
}