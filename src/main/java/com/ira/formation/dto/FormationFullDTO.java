package com.ira.formation.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormationFullDTO {

    private Long id;
    private String titre;
    private String description;

    private List<ModuleDTO> modules;
}