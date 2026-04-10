package com.ira.formation.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentDTO {

    private Long id;

    private String nom;

    private String filePath;

    private Long moduleId;
}