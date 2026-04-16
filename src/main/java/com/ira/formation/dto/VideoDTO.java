package com.ira.formation.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoDTO {

    private Long id;
    private String titre;
    private String filePath;

    private Long moduleId;
}