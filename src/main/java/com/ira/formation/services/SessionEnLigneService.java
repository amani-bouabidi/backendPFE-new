package com.ira.formation.services;

import com.ira.formation.dto.SessionEnLigneDTO;
import com.ira.formation.entities.Formation;
import com.ira.formation.entities.SessionEnLigne;
import com.ira.formation.entities.SessionStatus;
import com.ira.formation.entities.Utilisateur;
import com.ira.formation.repositories.FormationRepository;
import com.ira.formation.repositories.InscriptionRepository;
import com.ira.formation.repositories.SessionEnLigneRepository;
import com.ira.formation.repositories.UtilisateurRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionEnLigneService {

    private final SessionEnLigneRepository sessionRepository;
    private final FormationRepository formationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final InscriptionRepository inscriptionRepository;

    // =================== CREATE ===================
    @PreAuthorize("hasRole('FORMATEUR')")
    public SessionEnLigneDTO creerSession(Long formationId, String titre, String emailFormateur){

        Formation formation = formationRepository.findById(formationId)
                .orElseThrow(() -> new RuntimeException("Formation inexistante"));

        if (!formation.getFormateur().getEmail().equals(emailFormateur)) {
            throw new RuntimeException("Accès refusé");
        }

        SessionEnLigne session = new SessionEnLigne();
        session.setTitre(titre);
        session.setFormateurEmail(emailFormateur);
        session.setFormation(formation);
        session.setStatut(SessionStatus.EN_COURS);

        session.setLienReunion("https://meet.jit.si/"
                + titre.replaceAll(" ", "")
                + "-"
                + System.currentTimeMillis());

        return mapToDTO(sessionRepository.save(session));
    }

    // =================== GET BY FORMATEUR ===================
    @PreAuthorize("hasRole('FORMATEUR')")
    public List<SessionEnLigne> listerSessionsParFormateur(String emailFormateur) {
        return sessionRepository.findByFormateurEmail(emailFormateur);
    }

    // =================== GET SECURE ===================
    public SessionEnLigneDTO getSessionSecure(Long id, String email) {

        SessionEnLigne session = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session introuvable"));

        // 🔒 1. check session terminée
        if (session.getStatut().name().equals("TERMINE")) {
            throw new RuntimeException("Session terminée");
        }

        // 🔒 2. récupérer user
        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // 🔒 3. check inscription
        boolean inscrit = inscriptionRepository.existsByApprenantAndFormation(
                user,
                session.getFormation()
        );

        if (!inscrit) {
            throw new RuntimeException("Accès refusé : vous devez être inscrit");
        }

        return mapToDTO(session);
    }

    // =================== UPDATE ===================
    @PreAuthorize("hasRole('FORMATEUR')")
    public SessionEnLigne mettreAJourTitre(Long id, String nouveauTitre, String emailFormateur) {

        SessionEnLigne session = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session introuvable"));

        if (!session.getFormateurEmail().equals(emailFormateur)) {
            throw new RuntimeException("Accès refusé");
        }

        session.setTitre(nouveauTitre);
        return sessionRepository.save(session);
    }

    // =================== DELETE ===================
    @PreAuthorize("hasRole('FORMATEUR')")
    public void supprimerSession(Long id, String emailFormateur) {

        SessionEnLigne session = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session inexistante"));

        if (!session.getFormateurEmail().equals(emailFormateur)) {
            throw new RuntimeException("Accès refusé");
        }

        sessionRepository.delete(session);
    }

    // =================== TERMINER ===================
    @PreAuthorize("hasRole('FORMATEUR')")
    public SessionEnLigneDTO terminerSession(Long id, String emailFormateur) {

        SessionEnLigne session = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session introuvable"));

        if (!session.getFormateurEmail().equals(emailFormateur)) {
            throw new RuntimeException("Accès refusé");
        }

        session.setStatut(SessionStatus.TERMINE);

        return mapToDTO(sessionRepository.save(session));
    }

    // =================== DTO ===================
    public SessionEnLigneDTO mapToDTO(SessionEnLigne session) {
        return SessionEnLigneDTO.builder()
                .titre(session.getTitre())
                .lienReunion(session.getLienReunion())
                .statut(session.getStatut().name())
                .build();
    }
    
    
    
    
}