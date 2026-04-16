package com.ira.formation.dto;

import lombok.*;
import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleDTO {

    private Long id;

    private String titre;

    private String description;

    private Long formationId;   
    
    private List<DocumentDTO> documents;
    private List<VideoDTO> videos;
}