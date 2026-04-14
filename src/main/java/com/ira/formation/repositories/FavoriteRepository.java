package com.ira.formation.repositories;

import com.ira.formation.entities.Favorite;
import com.ira.formation.entities.FavoriteType;
import com.ira.formation.entities.Utilisateur;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    // 🔍 get all favorites of user
    List<Favorite> findByUser(Utilisateur user);

    // 🔍 check if already favorite
    boolean existsByUserAndElementIdAndType(
            Utilisateur user,
            Long elementId,
            FavoriteType type
    );

    // 🔍 find one favorite (important for toggle)
    Optional<Favorite> findByUserAndElementIdAndType(
            Utilisateur user,
            Long elementId,
            FavoriteType type
    );
}