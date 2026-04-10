package com.ira.formation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponseDTO {
    private String jwt;           // Token principal
    private String refreshToken;  // Refresh token
    private String role;          // Rôle de l’utilisateur
}
