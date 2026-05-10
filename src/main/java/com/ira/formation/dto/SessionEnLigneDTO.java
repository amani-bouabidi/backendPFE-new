package com.ira.formation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionEnLigneDTO {

    private Long id;
    private Long formationId;
    private String formationTitre;
    private String titre;
    private String lienReunion;
    private String statut;
}