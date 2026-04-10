package com.ira.formation.repositories;

import com.ira.formation.entities.Domaine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DomaineRepository extends JpaRepository<Domaine, Long> {
	boolean existsByNom(String nom);
}