package com.ira.formation.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomaineStatsDTO {

    private Long domaineId;

    private String nomDomaine;

    private Long nombreFormations;
}