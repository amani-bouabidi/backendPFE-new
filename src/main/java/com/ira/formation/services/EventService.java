package com.ira.formation.services;

import com.ira.formation.dto.EventDTO;
import com.ira.formation.entities.Event;
import com.ira.formation.entities.Formation;
import com.ira.formation.entities.Utilisateur;
import com.ira.formation.repositories.EventRepository;
import com.ira.formation.repositories.FormationRepository;
import com.ira.formation.repositories.InscriptionRepository;
import com.ira.formation.repositories.UtilisateurRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final FormationRepository formationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final InscriptionRepository inscriptionRepository;

    // =================== CREATE EVENT (FORMATEUR ONLY) ===================
    @PreAuthorize("hasRole('FORMATEUR')")
    public EventDTO creerEvent(EventDTO dto, String emailFormateur) {

        Formation formation = formationRepository.findById(dto.getFormationId())
                .orElseThrow(() -> new RuntimeException("Formation inexistante"));

        // Vérification: le formateur ne peut créer des événements que pour SES formations
        if (!formation.getFormateur().getEmail().equals(emailFormateur)) {
            throw new RuntimeException("Accès refusé : cette formation ne vous appartient pas");
        }

        Event event = new Event();
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setDate(dto.getDate());
        event.setTime(dto.getTime());
        event.setFormation(formation);

        return mapToDTO(eventRepository.save(event));
    }

    // =================== GET EVENTS FOR APPRENANT (via inscriptions) ===================
    @PreAuthorize("hasRole('APPRENANT')")
    public List<EventDTO> getMesEvents(String email) {

        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Retourne les événements de toutes les formations auxquelles l'apprenant est inscrit
        return inscriptionRepository.findByApprenant(user)
                .stream()
                .flatMap(inscription ->
                        eventRepository.findByFormation(inscription.getFormation()).stream()
                )
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // =================== BUG FIX N°9: GET EVENTS FOR FORMATEUR (via SES formations) ===================
    @PreAuthorize("hasRole('FORMATEUR')")
    public List<EventDTO> getMesEventsFormateur(String email) {

        Utilisateur formateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Formateur non trouvé"));

        // Récupère les événements de toutes les formations dont il est responsable
        return formationRepository.findByFormateur(formateur)
                .stream()
                .flatMap(formation ->
                        eventRepository.findByFormation(formation).stream()
                )
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // =================== MAPPER DTO ===================
    public EventDTO mapToDTO(Event event) {
        return EventDTO.builder()
                .id(event.getId())
                .title(event.getTitle())
                .date(event.getDate())
                .time(event.getTime())
                .description(event.getDescription())
                .formationId(event.getFormation().getId())
                .build();
    }
}
