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

    // FIX N°2: Added missing fields
    private Long formateurId;
    private String formateurNom;
    private Long domaineId;
    private String domaineNom;

    private List<ModuleDTO> modules;
}
