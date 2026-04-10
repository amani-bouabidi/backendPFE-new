package com.ira.formation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

    @Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public class DomaineDTO {

	    private Long id;
	    private String nom;
	}

