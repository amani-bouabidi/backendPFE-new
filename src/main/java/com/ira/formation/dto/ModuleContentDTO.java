package com.ira.formation.dto;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleContentDTO {

    private String moduleTitre;

    private List<DocumentDTO> documents;

    private List<VideoDTO> videos;

}