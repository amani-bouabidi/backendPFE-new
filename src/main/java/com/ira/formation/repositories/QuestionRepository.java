package com.ira.formation.repositories;

import com.ira.formation.entities.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {
	boolean existsByTestIdAndTexte(Long testId, String texte);
}