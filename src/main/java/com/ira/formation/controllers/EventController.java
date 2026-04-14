package com.ira.formation.controllers;

import com.ira.formation.dto.EventDTO;
import com.ira.formation.services.EventService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    // =================== CREATE EVENT (FORMATEUR) ===================
    @PostMapping
    public EventDTO createEvent(@RequestBody EventDTO dto, Authentication authentication) {

        String email = authentication.getName();

        return eventService.creerEvent(dto, email);
    }

    // =================== GET MY EVENTS (APPRENANT) ===================
    @GetMapping("/my")
    public List<EventDTO> getMyEvents(Authentication authentication) {

        String email = authentication.getName();

        return eventService.getMesEvents(email);
    }
}