package com.ira.formation.repositories;

import com.ira.formation.entities.Formation;
import com.ira.formation.entities.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ira.formation.entities.Domaine;

import java.util.List;

@Repository
public interface FormationRepository extends JpaRepository<Formation, Long> {

    // Toutes les formations créées par un formateur spécifique
    List<Formation> findByFormateur(Utilisateur formateur);

    // Optionnel: pour filtrer par titre
    List<Formation> findByTitreContainingIgnoreCase(String titre);
    
    List<Formation> findByDomaine(Domaine domaine);
    
    boolean existsByTitreAndFormateur(String titre, Utilisateur formateur);
}