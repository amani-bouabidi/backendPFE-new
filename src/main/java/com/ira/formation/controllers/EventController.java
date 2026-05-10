package com.ira.formation.controllers;

import com.ira.formation.dto.EventDTO;
import com.ira.formation.services.EventService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class EventController {

    private final EventService eventService;

    // =================== BUG FIX N°8: CREATE EVENT - FORMATEUR ONLY ===================
    @PostMapping
    @PreAuthorize("hasRole('FORMATEUR')")
    public EventDTO createEvent(@RequestBody EventDTO dto, Authentication authentication) {

        String email = authentication.getName();
        return eventService.creerEvent(dto, email);
    }

    // =================== GET MY EVENTS - APPRENANT (via inscriptions) ===================
    @GetMapping("/my")
    @PreAuthorize("hasRole('APPRENANT')")
    public List<EventDTO> getMyEvents(Authentication authentication) {

        String email = authentication.getName();
        return eventService.getMesEvents(email);
    }

    // =================== BUG FIX N°9: GET MY EVENTS - FORMATEUR (via ses formations) ===================
    @GetMapping("/formateur")
    @PreAuthorize("hasRole('FORMATEUR')")
    public List<EventDTO> getMyEventsFormateur(Authentication authentication) {

        String email = authentication.getName();
        return eventService.getMesEventsFormateur(email);
    }
}
