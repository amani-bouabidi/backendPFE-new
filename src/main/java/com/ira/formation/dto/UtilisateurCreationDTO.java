package com.ira.formation.dto;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UtilisateurCreationDTO {
	@NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @Email(message = "Email invalide")
    @NotBlank(message = "L'email est obligatoire")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Mot de passe trop court (min 8)")
    private String password;

    @NotBlank(message = "Le rôle est obligatoire")
    private String roleNom;   // ex: "FORMATEUR" ou "APPRENANT"


}
