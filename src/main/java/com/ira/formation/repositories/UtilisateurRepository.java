package com.ira.formation.repositories;

import com.ira.formation.entities.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByEmail(String email);

    // Ajouté : pour getAllFormateurs() et getAllApprenants()
    List<Utilisateur> findByRoleNom(String roleNom);

    // Ajouté : pour vérifier l'unicité lors de la création / mise à jour
    boolean existsByEmail(String email);

    // Optionnel mais utile plus tard (si besoin) :
    // boolean existsByEmailAndIdNot(String email, Long id);  // pour update sans faux positif
}