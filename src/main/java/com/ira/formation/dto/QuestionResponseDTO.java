package com.ira.formation.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QuestionResponseDTO {

    private Long id;
    private String texte;
    private Long testId;
    private List<ChoixResponseDTO> choix;
}