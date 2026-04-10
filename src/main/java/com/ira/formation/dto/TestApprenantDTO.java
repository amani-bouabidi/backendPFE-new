package com.ira.formation.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TestApprenantDTO {
    private Long id;
    private String titre;
    private Long formationId;
    private String formationTitre;
    private List<QuestionApprenantDTO> questions;
}