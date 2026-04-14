package com.ira.formation.repositories;

import com.ira.formation.entities.ModuleCompletion;
import com.ira.formation.entities.Utilisateur;
import com.ira.formation.entities.Module;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ModuleCompletionRepository extends JpaRepository<ModuleCompletion, Long> {

    long countByApprenantAndCompletedTrue(Utilisateur apprenant);

    boolean existsByApprenantAndModule(Utilisateur apprenant, Module module);

    long countByApprenant_IdAndModule_Formation_IdAndCompletedTrue(Long apprenantId, Long formationId);
}