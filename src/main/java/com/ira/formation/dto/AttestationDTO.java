package com.ira.formation.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttestationDTO {

    private Long id;
    private String filePath;
    private String apprenantNom;
    private String formationTitre;
    private String createdAt;
}