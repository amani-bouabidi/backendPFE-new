package com.ira.formation.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FormationPublicDTO {
    private Long id;
    private String titre;
    private String description;
}