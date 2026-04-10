package com.ira.formation.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomaineFormationsDTO {

    private Long domaineId;
    private String nomDomaine;

    private List<FormationDTO> formations;
}