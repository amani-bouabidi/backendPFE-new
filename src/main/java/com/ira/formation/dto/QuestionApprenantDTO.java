package com.ira.formation.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class QuestionApprenantDTO {
    private Long id;
    private String texte;
    private Long testId;
    private List<ChoixApprenantDTO> choix;
}