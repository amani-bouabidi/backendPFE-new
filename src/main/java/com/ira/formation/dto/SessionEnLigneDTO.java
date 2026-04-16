package com.ira.formation.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SessionEnLigneDTO {

    private Long id;
    private Long formationId;
    private String titre;
    private String lienReunion;
    private String statut;
}