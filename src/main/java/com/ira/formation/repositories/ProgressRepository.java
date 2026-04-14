package com.ira.formation.repositories;

import com.ira.formation.entities.Progress;
import com.ira.formation.entities.Utilisateur;
import com.ira.formation.entities.Formation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface ProgressRepository extends JpaRepository<Progress, Long> {

    Optional<Progress> findByApprenantAndFormation(Utilisateur apprenant, Formation formation);

    List<Progress> findByFormation(Formation formation);
}