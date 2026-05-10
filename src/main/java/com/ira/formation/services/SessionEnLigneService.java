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
    // FIX BUG #2 — injection du service de notification
    private final NotificationService notificationService;

    // =================== CREATE ===================
    public SessionEnLigneDTO creerSession(Long formationId, String titre, String emailFormateur) {

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

        SessionEnLigne saved = sessionRepository.save(session);

        // FIX BUG #2 — Notifier tous les apprenants inscrits à cette formation
        inscriptionRepository.findAll().stream()
            .filter(insc -> insc.getFormation().getId().equals(formationId) && insc.isValide())
            .forEach(insc -> {
                    String message = String.format(
                        "📡 Nouvelle session en ligne : \"%s\" pour la formation \"%s\". Lien : %s",
                        titre,
                        formation.getTitre(),
                        saved.getLienReunion()
                    );
                    notificationService.createNotification(insc.getApprenant(), message);
                });

        return mapToDTO(saved);
    }

    // =================== FORMATEUR LIST ===================
    public List<SessionEnLigneDTO> listerSessionsParFormateur(String emailFormateur) {

        Utilisateur formateur = utilisateurRepository.findByEmail(emailFormateur)
                .orElseThrow(() -> new RuntimeException("Formateur non trouvé"));

        // Récupérer toutes les formations du formateur, puis toutes leurs sessions
        // (plus fiable que findByFormateurEmail qui peut être null pour les anciennes sessions)
        List<Formation> formations = formationRepository.findByFormateur(formateur);

        return formations.stream()
                .flatMap(f -> sessionRepository.findByFormation(f).stream())
                .map(this::mapToDTO)
                .distinct()
                .toList();
    }

    // =================== APPRENANT - sessions de ses formations inscrites ===================
    public List<SessionEnLigneDTO> getSessionsByFormationForApprenant(Long formationId, String email) {

        Formation formation = formationRepository.findById(formationId)
                .orElseThrow(() -> new RuntimeException("Formation introuvable"));

        Utilisateur apprenant = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!inscriptionRepository.existsByApprenantAndFormation(apprenant, formation)) {
            throw new RuntimeException("Accès refusé : vous n'êtes pas inscrit à cette formation");
        }

        return sessionRepository.findByFormation(formation)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    // =================== APPRENANT - toutes les sessions de toutes ses formations ===================
    public List<SessionEnLigneDTO> getAllSessionsForApprenant(String email) {

        Utilisateur apprenant = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // On prend TOUTES les inscriptions (valides ou non) pour ne rien rater
        // Un apprenant inscrit même sans validation doit voir les sessions
        return inscriptionRepository.findByApprenant(apprenant)
                .stream()
                .flatMap(inscription ->
                    sessionRepository.findByFormation(inscription.getFormation()).stream()
                )
                .map(this::mapToDTO)
                .distinct()
                .toList();
    }

    // =================== GET SECURE (FORMATEUR ou APPRENANT avec vérification) ===================
    public SessionEnLigneDTO getSessionSecure(Long id, String email) {

        SessionEnLigne session = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session introuvable"));

        if (session.getStatut() == SessionStatus.TERMINE) {
            throw new RuntimeException("Session terminée");
        }

        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

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
                .formationTitre(session.getFormation().getTitre())
                .titre(session.getTitre())
                .lienReunion(session.getLienReunion())
                .statut(session.getStatut().name())
                .build();
    }
}