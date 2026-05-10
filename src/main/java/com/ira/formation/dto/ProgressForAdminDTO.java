package com.ira.formation.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressForAdminDTO {

    // Informations sur l'apprenant
    private Long apprenantId;
    private String apprenantNom;
    private String apprenantPrenom;
    private String apprenantEmail;

    // Informations sur la formation
    private Long formationId;
    private String formationTitre;
    private String formateurNom;

    // Progression
    private double percentage;
    private boolean completed;
}
