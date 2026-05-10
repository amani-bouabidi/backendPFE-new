package com.ira.formation.services;

import com.ira.formation.dto.UtilisateurCreationDTO;
import com.ira.formation.dto.UtilisateurUpdateDTO;
import com.ira.formation.dto.UtilisateurResponseDTO;
import com.ira.formation.entities.Role;
import com.ira.formation.entities.Utilisateur;
import com.ira.formation.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    // FIX BUG #7 — ajout des repositories nécessaires pour supprimer les données liées
    private final InscriptionRepository inscriptionRepository;
    private final ProgressRepository progressRepository;
    private final FavoriteRepository favoriteRepository;
    private final AttestationRepository attestationRepository;
    private final NotificationRepository notificationRepository;
    private final ModuleCompletionRepository moduleCompletionRepository;

    // =====================================================
    // CREATE FORMATEUR
    // =====================================================
    @Transactional
    public UtilisateurResponseDTO creerFormateur(UtilisateurCreationDTO dto) {

        if (utilisateurRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email déjà utilisé");
        }

        Role role = roleRepository.findByNom("FORMATEUR")
                .orElseThrow(() -> new IllegalArgumentException("Rôle FORMATEUR introuvable"));

        Utilisateur utilisateur = Utilisateur.builder()
                .nom(dto.getNom())
                .prenom(dto.getPrenom())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(role)
                .actif(true)
                .build();

        return mapToDTO(utilisateurRepository.save(utilisateur));
    }

    // =====================================================
    // UPDATE FORMATEUR
    // =====================================================
    @Transactional
    public UtilisateurResponseDTO modifierFormateur(Long id, UtilisateurUpdateDTO dto) {

        Utilisateur user = utilisateurRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Formateur non trouvé"));

        if (!"FORMATEUR".equals(user.getRole().getNom())) {
            throw new IllegalArgumentException("Ce compte n'est pas un formateur");
        }

        if (dto.getNom() != null) user.setNom(dto.getNom());
        if (dto.getPrenom() != null) user.setPrenom(dto.getPrenom());

        if (dto.getEmail() != null) {
            if (!dto.getEmail().equals(user.getEmail())
                    && utilisateurRepository.existsByEmail(dto.getEmail())) {
                throw new IllegalArgumentException("Email déjà utilisé");
            }
            user.setEmail(dto.getEmail());
        }

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        return mapToDTO(utilisateurRepository.save(user));
    }

    // =====================================================
    // DELETE FORMATEUR
    // =====================================================
    @Transactional
    public void supprimerFormateur(Long id) {

        Utilisateur user = utilisateurRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Formateur non trouvé"));

        if (!"FORMATEUR".equals(user.getRole().getNom())) {
            throw new IllegalArgumentException("Ce n'est pas un formateur");
        }

        refreshTokenRepository.deleteByUtilisateurId(id);
        utilisateurRepository.delete(user);
    }

    // =====================================================
    // DELETE APPRENANT  ← FIX BUG #7
    // Supprime toutes les données liées avant de supprimer l'utilisateur
    // pour éviter les erreurs de contrainte FK
    // =====================================================
    @Transactional
    public void supprimerApprenant(Long id) {

        Utilisateur user = utilisateurRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Apprenant non trouvé"));

        if (!"APPRENANT".equals(user.getRole().getNom())) {
            throw new IllegalArgumentException("Ce n'est pas un apprenant");
        }

        // 1. Supprimer les tokens de rafraîchissement
        refreshTokenRepository.deleteByUtilisateurId(id);

        // 2. Supprimer les notifications
        notificationRepository.deleteAll(
            notificationRepository.findByUtilisateurIdOrderByCreatedAtDesc(id)
        );

        // 3. Supprimer les favoris
        favoriteRepository.deleteAll(
            favoriteRepository.findByUser(user)
        );

        // 4. Supprimer les complétions de modules
        moduleCompletionRepository.deleteAll(
            moduleCompletionRepository.findAll().stream()
                .filter(mc -> mc.getApprenant().getId().equals(id))
                .toList()
        );

        // 5. Supprimer les progressions
        progressRepository.deleteAll(
            progressRepository.findAll().stream()
                .filter(p -> p.getApprenant().getId().equals(id))
                .toList()
        );

        // 6. Supprimer les attestations
        attestationRepository.deleteAll(
            attestationRepository.findAll().stream()
                .filter(a -> a.getApprenant().getId().equals(id))
                .toList()
        );

        // 7. Supprimer les inscriptions (test de validation)
        inscriptionRepository.deleteAll(
            inscriptionRepository.findByApprenant(user)
        );

        // 8. Supprimer l'utilisateur
        utilisateurRepository.delete(user);
    }

    // =====================================================
    // LISTES
    // =====================================================
    public List<UtilisateurResponseDTO> getAllFormateurs() {
        return utilisateurRepository.findByRoleNom("FORMATEUR")
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public List<UtilisateurResponseDTO> getAllApprenants() {
        return utilisateurRepository.findByRoleNom("APPRENANT")
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    // =====================================================
    // MAPPER
    // =====================================================
    private UtilisateurResponseDTO mapToDTO(Utilisateur u) {
        return UtilisateurResponseDTO.builder()
                .id(u.getId())
                .nom(u.getNom())
                .prenom(u.getPrenom())
                .email(u.getEmail())
                .roleNom(u.getRole().getNom())
                .actif(u.isActif())
                .createdAt(u.getCreatedAt() != null ? u.getCreatedAt().toString() : null)
                .build();
    }
}
