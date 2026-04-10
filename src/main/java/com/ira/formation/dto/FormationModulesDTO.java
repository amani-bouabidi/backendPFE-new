package com.ira.formation.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormationModulesDTO {

    private Long formationId;

    private String titreFormation;

    private List<ModuleDTO> modules;

}