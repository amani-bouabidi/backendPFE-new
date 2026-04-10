// src/main/java/com/ira/formation/services/RefreshTokenService.java
package com.ira.formation.services;

import com.ira.formation.entities.RefreshToken;
import com.ira.formation.entities.Utilisateur;
import com.ira.formation.repositories.RefreshTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private static final long REFRESH_TOKEN_DURATION = 7 * 24 * 60 * 60; // 7 jours

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public RefreshToken createRefreshToken(Utilisateur utilisateur) {
        // ✅ ÉTAPE 1 : Supprimer l'ancien token s'il existe (pour éviter duplicate key)
        refreshTokenRepository.deleteByUtilisateurId(utilisateur.getId());
        
        // ✅ ÉTAPE 2 : Créer un nouveau token
        RefreshToken token = new RefreshToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUtilisateur(utilisateur);
        token.setCreatedAt(Instant.now());
        token.setExpiresAt(Instant.now().plusSeconds(REFRESH_TOKEN_DURATION));
        token.setRevoked(false);
        
        return refreshTokenRepository.save(token);
    }

    public Optional<RefreshToken> getRefreshTokenByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public boolean isValid(RefreshToken token) {
        return !token.isRevoked() && token.getExpiresAt().isAfter(Instant.now());
    }

    @Transactional
    public void revokeToken(RefreshToken token) {
        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }
}