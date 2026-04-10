package com.ira.formation.repositories;

import com.ira.formation.entities.SessionEnLigne;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SessionEnLigneRepository extends JpaRepository<SessionEnLigne, Long> {
    List<SessionEnLigne> findByFormateurEmail(String formateurEmail);
}