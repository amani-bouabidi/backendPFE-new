package com.ira.formation.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleDTO {

    private Long id;

    private String titre;

    private String description;

    private Long formationId;

}