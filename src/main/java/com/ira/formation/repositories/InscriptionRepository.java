package com.ira.formation.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import com.ira.formation.entities.Inscription;
import com.ira.formation.entities.Utilisateur;
import com.ira.formation.entities.Formation;

public interface InscriptionRepository extends JpaRepository<Inscription, Long> {

    boolean existsByApprenantAndFormation(Utilisateur apprenant, Formation formation);

    boolean existsByApprenantAndFormationAndValide(Utilisateur apprenant, Formation formation, boolean valide);

    Optional<Inscription> findByApprenantAndFormation(Utilisateur apprenant, Formation formation);
}