package com.ira.formation.services;

import com.ira.formation.dto.NotificationResponseDTO;
import com.ira.formation.entities.Notification;
import com.ira.formation.entities.Utilisateur;
import com.ira.formation.repositories.NotificationRepository;
import com.ira.formation.repositories.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UtilisateurRepository utilisateurRepository;

    // ✅ GET notifications
    public List<NotificationResponseDTO> getUserNotifications(String email){

        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        return notificationRepository
                .findByUtilisateurIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(n -> NotificationResponseDTO.builder()
                        .id(n.getId())
                        .message(n.getMessage())
                        .read(n.isRead())
                        .createdAt(n.getCreatedAt())
                        .build())
                .toList();
    }
    // ✅ CREATE notification (باش نستعملوها بعد)
    public void createNotification(Utilisateur user, String message){

        Notification notif = Notification.builder()
                .message(message)
                .utilisateur(user)
                .build();

        notificationRepository.save(notif);
    }
    
    
    public void markAsRead(Long notificationId, String email){

        Notification notif = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification non trouvée"));

        // ✅ sécurité: فقط صاحبها ينجم يبدلها
        if(!notif.getUtilisateur().getEmail().equals(email)){
            throw new RuntimeException("Accès refusé");
        }

        notif.setRead(true);

        notificationRepository.save(notif);
    }
}