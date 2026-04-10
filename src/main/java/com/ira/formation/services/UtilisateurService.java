package com.ira.formation.services;

import com.ira.formation.dto.UtilisateurCreationDTO;
import com.ira.formation.dto.UtilisateurUpdateDTO;
import com.ira.formation.dto.UtilisateurResponseDTO;
import com.ira.formation.entities.Role;
import com.ira.formation.entities.Utilisateur;
import com.ira.formation.repositories.RefreshTokenRepository;
import com.ira.formation.repositories.RoleRepository;
import com.ira.formation.repositories.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor   // remplace le constructor injection manuel
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    // =====================================================================
    //                  Création (surtout pour admin)
    // =====================================================================

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")   // ← à activer quand Security sera configuré
    public UtilisateurResponseDTO creerFormateur(UtilisateurCreationDTO dto) {
        return creerUtilisateur(dto, "FORMATEUR");
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public UtilisateurResponseDTO creerApprenant(UtilisateurCreationDTO dto) {
        return creerUtilisateur(dto, "APPRENANT");
    }

    // Méthode interne réutilisable
    private UtilisateurResponseDTO creerUtilisateur(UtilisateurCreationDTO dto, String roleAttendu) {
        if (utilisateurRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Cet email est déjà utilisé");
        }

        Role role = roleRepository.findByNom(roleAttendu)
                .orElseThrow(() -> new IllegalArgumentException("Rôle " + roleAttendu + " introuvable"));

        // Vérification cohérence (optionnel mais utile)
        if (!roleAttendu.equals(dto.getRoleNom())) {
            throw new IllegalArgumentException("Le rôle demandé ne correspond pas à l'action");
        }

        Utilisateur utilisateur = Utilisateur.builder()
                .nom(dto.getNom())
                .prenom(dto.getPrenom())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(role)
                .actif(true)
                .build();

        Utilisateur saved = utilisateurRepository.save(utilisateur);

        return mapToResponseDTO(saved);
    }

    // =====================================================================
    //                  Mise à jour
    // =====================================================================

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public UtilisateurResponseDTO modifierFormateur(Long id, UtilisateurUpdateDTO dto) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Formateur non trouvé"));

        if (!"FORMATEUR".equals(utilisateur.getRole().getNom())) {
            throw new IllegalArgumentException("Cet ID ne correspond pas à un formateur");
        }

        applyUpdate(utilisateur, dto);
        Utilisateur saved = utilisateurRepository.save(utilisateur);

        return mapToResponseDTO(saved);
    }

    private void applyUpdate(Utilisateur utilisateur, UtilisateurUpdateDTO dto) {
        if (dto.getNom() != null) utilisateur.setNom(dto.getNom());
        if (dto.getPrenom() != null) utilisateur.setPrenom(dto.getPrenom());
        if (dto.getEmail() != null) {
            if (!dto.getEmail().equals(utilisateur.getEmail()) &&
                    utilisateurRepository.existsByEmail(dto.getEmail())) {
                throw new IllegalArgumentException("Email déjà utilisé");
            }
            utilisateur.setEmail(dto.getEmail());
        }
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            utilisateur.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        if (dto.getRoleNom() != null) {
            Role newRole = roleRepository.findByNom(dto.getRoleNom())
                    .orElseThrow(() -> new IllegalArgumentException("Rôle introuvable"));
            utilisateur.setRole(newRole);
        }
    }

    // =====================================================================
    //                  Suppression
    // =====================================================================

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void supprimerFormateur(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Formateur non trouvé"));

        if (!"FORMATEUR".equals(utilisateur.getRole().getNom())) {
            throw new IllegalArgumentException("Cet ID ne correspond pas à un formateur");
        }

        utilisateurRepository.delete(utilisateur);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void supprimerApprenant(Long id) {

        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Apprenant non trouvé"));

        if (!"APPRENANT".equals(utilisateur.getRole().getNom())) {
            throw new IllegalArgumentException("Cet ID ne correspond pas à un apprenant");
        }

        // ✅ مهم جدا
        refreshTokenRepository.deleteByUtilisateurId(id);

        utilisateurRepository.delete(utilisateur);
    }

    // =====================================================================
    //                  Méthodes utilitaires
    // =====================================================================

    public List<UtilisateurResponseDTO> getAllFormateurs() {
        return utilisateurRepository.findByRoleNom("FORMATEUR")
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<UtilisateurResponseDTO> getAllApprenants() {
        return utilisateurRepository.findByRoleNom("APPRENANT")
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    private UtilisateurResponseDTO mapToResponseDTO(Utilisateur u) {
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