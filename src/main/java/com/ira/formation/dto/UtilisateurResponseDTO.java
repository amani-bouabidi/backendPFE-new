package com.ira.formation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//Pour les réponses (ne jamais exposer password ni données sensibles)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UtilisateurResponseDTO {
 private Long id;
 private String nom;
 private String prenom;
 private String email;
 private String roleNom;
 private boolean actif;
 private String createdAt;   // ou LocalDateTime si tu préfères
}