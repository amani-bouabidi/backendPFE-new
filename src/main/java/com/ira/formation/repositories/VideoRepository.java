package com.ira.formation.repositories;

import com.ira.formation.entities.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.ira.formation.entities.Module;

public interface VideoRepository extends JpaRepository<Video, Long> {
	List<Video> findByModule(Module module);
}