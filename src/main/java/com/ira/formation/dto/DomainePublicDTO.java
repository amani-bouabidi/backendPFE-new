package com.ira.formation.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DomainePublicDTO {
    private Long id;
    private String nom;
    private List<FormationPublicDTO> formations;
}