package com.ira.formation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UtilisateurUpdateDTO {

    private String nom;
    private String prenom;
    private String email;
    private String password;
}
