package com.ira.formation.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDTO {

    private Long id;
    private String nom;
    private String filePath;

    private Long moduleId; 
}