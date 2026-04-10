package com.ira.formation.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ira.formation.entities.Event;
import com.ira.formation.entities.Formation;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // pour récupérer les events d'une formation donnée
    List<Event> findByFormation(Formation formation);

}