package com.ira.formation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteResponseDTO {

    private Long elementId;

    private String type;

    private Object content; // module / video / document
}