package com.ira.formation.repositories;

import com.ira.formation.entities.Attestation;
import com.ira.formation.entities.Formation;
import com.ira.formation.entities.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttestationRepository extends JpaRepository<Attestation, Long> {

    Optional<Attestation> findByApprenantAndFormation(Utilisateur apprenant, Formation formation);
}