package com.ira.formation.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressDTO {

    private Long formationId;
    private Long moduleId;
    private double percentage;
    private boolean completed;
}