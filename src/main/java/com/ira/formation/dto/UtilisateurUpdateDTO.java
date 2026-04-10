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

    // password optionnel → on l'encode seulement s'il est envoyé
    private String password;

    private String roleNom;   // optionnel aussi

}
