package com.ira.formation.repositories;
import java.util.Optional;
import com.ira.formation.entities.Test;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestRepository extends JpaRepository<Test, Long> {
	boolean existsByFormationId(Long formationId);
	Optional<Test> findByFormationId(Long formationId);
}