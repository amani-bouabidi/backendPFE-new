package com.ira.formation.repositories;

import com.ira.formation.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUtilisateurIdOrderByCreatedAtDesc(Long userId);
}