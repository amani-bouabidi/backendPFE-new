// src/main/java/com/ira/formation/repositories/RefreshTokenRepository.java
package com.ira.formation.repositories;

import com.ira.formation.entities.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    
    Optional<RefreshToken> findByToken(String token);
    
    // ✅ AJOUTER CETTE MÉTHODE (pour supprimer l'ancien token)
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.utilisateur.id = :utilisateurId")
    void deleteByUtilisateurId(Long utilisateurId);
    
    // Optionnel : trouver par utilisateur
    Optional<RefreshToken> findByUtilisateurId(Long utilisateurId);
}