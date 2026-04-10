package com.ira.formation.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoDTO {

    private Long id;

    private String titre;

    private String filePath;

    private Long moduleId;
}