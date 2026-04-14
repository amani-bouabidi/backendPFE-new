package com.ira.formation.repositories;

import com.ira.formation.entities.Document;
import com.ira.formation.entities.Module;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {
	List<Document> findByModule(Module module);
}