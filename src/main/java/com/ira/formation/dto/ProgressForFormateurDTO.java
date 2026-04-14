package com.ira.formation.dto;

import lombok.*;

@Data
@Builder
public class ProgressForFormateurDTO {

    private Long apprenantId;
    private String apprenantNom;
    private String apprenantPrenom;

    private Long formationId;

    private double percentage;
    private boolean completed;
}