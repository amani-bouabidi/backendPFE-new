package com.ira.formation.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TestResponseDTO {
    private Long id;
    private String titre;
    private Long formationId;
    private String formationTitre; 
    private List<QuestionResponseDTO> questions;
}