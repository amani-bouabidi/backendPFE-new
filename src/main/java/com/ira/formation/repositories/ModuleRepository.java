package com.ira.formation.repositories;

import com.ira.formation.entities.Module;
import com.ira.formation.entities.Formation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModuleRepository extends JpaRepository<Module, Long> {

    List<Module> findByFormation(Formation formation);

}