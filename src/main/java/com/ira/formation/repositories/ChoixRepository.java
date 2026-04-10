package com.ira.formation.repositories;

import com.ira.formation.entities.Choix;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChoixRepository extends JpaRepository<Choix, Long> {
	boolean existsByQuestionIdAndTexte(Long questionId, String texte);
}