package com.ira.formation.repositories;

import com.ira.formation.entities.Formation;
import com.ira.formation.entities.SessionEnLigne;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SessionEnLigneRepository extends JpaRepository<SessionEnLigne, Long> {

    // Pour récupérer les sessions d'un formateur
    List<SessionEnLigne> findByFormateurEmail(String formateurEmail);

    // BUG FIX N°3 + Composition delete: pour récupérer les sessions d'une formation
    List<SessionEnLigne> findByFormation(Formation formation);
}
