package com.ira.formation.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormationDTO {

    private Long id;

    private String titre;

    private String description;

    private Long formateurId;

    private String formateurNom;
    
    private Long domaineId;
    
    private String domaineNom;
}