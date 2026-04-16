package com.ira.formation.services;

import com.ira.formation.dto.SessionEnLigneDTO;
import com.ira.formation.entities.*;
import com.ira.formation.repositories.*;
import lombok.RequiredArgsConstructor;
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

        session.setLienReunion(
                "https://meet.jit.si/" +
                titre.replaceAll(" ", "") +
                "-" +
                System.currentTimeMillis()
        );

        return mapToDTO(sessionRepository.save(session));
    }

    // =================== FORMATEUR LIST ===================
    public List<SessionEnLigneDTO> listerSessionsParFormateur(String emailFormateur) {
        return sessionRepository.findByFormateurEmail(emailFormateur)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    // =================== GET SECURE ===================
    public SessionEnLigneDTO getSessionSecure(Long id, String email) {

        SessionEnLigne session = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session introuvable"));

        if (session.getStatut() == SessionStatus.TERMINE) {
            throw new RuntimeException("Session terminée");
        }

        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // APPRENANT check seulement
        if ("APPRENANT".equals(user.getRole().getNom())) {

            boolean inscrit = inscriptionRepository.existsByApprenantAndFormation(
                    user,
                    session.getFormation()
            );

            if (!inscrit) {
                throw new RuntimeException("Accès refusé : vous devez être inscrit");
            }
        }

        return mapToDTO(session);
    }

    // =================== UPDATE ===================
    public SessionEnLigneDTO mettreAJourTitre(Long id, String nouveauTitre, String emailFormateur) {

        SessionEnLigne session = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session introuvable"));

        if (!session.getFormateurEmail().equals(emailFormateur)) {
            throw new RuntimeException("Accès refusé");
        }

        session.setTitre(nouveauTitre);
        return mapToDTO(sessionRepository.save(session));
    }

    // =================== DELETE ===================
    public void supprimerSession(Long id, String emailFormateur) {

        SessionEnLigne session = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session inexistante"));

        if (!session.getFormateurEmail().equals(emailFormateur)) {
            throw new RuntimeException("Accès refusé");
        }

        sessionRepository.delete(session);
    }

    // =================== TERMINER ===================
    public SessionEnLigneDTO terminerSession(Long id, String emailFormateur) {

        SessionEnLigne session = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session introuvable"));

        if (!session.getFormateurEmail().equals(emailFormateur)) {
            throw new RuntimeException("Accès refusé");
        }

        session.setStatut(SessionStatus.TERMINE);
        return mapToDTO(sessionRepository.save(session));
    }

    // =================== MAPPER ===================
    public SessionEnLigneDTO mapToDTO(SessionEnLigne session) {
        return SessionEnLigneDTO.builder()
                .id(session.getId())
                .formationId(session.getFormation().getId())
                .titre(session.getTitre())
                .lienReunion(session.getLienReunion())
                .statut(session.getStatut().name())
                .build();
    }
}