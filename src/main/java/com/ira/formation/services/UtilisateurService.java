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
    private final PasswordEncoder passwordEncoder; // ✅ IMPORTANT

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
                .password(passwordEncoder.encode(dto.getPassword())) // ✅ HASH
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
            user.setPassword(passwordEncoder.encode(dto.getPassword())); // ✅ HASH
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

        utilisateurRepository.delete(user);
    }

    // =====================================================
    // DELETE APPRENANT
    // =====================================================
    @Transactional
    public void supprimerApprenant(Long id) {

        Utilisateur user = utilisateurRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Apprenant non trouvé"));

        if (!"APPRENANT".equals(user.getRole().getNom())) {
            throw new IllegalArgumentException("Ce n'est pas un apprenant");
        }

        refreshTokenRepository.deleteByUtilisateurId(id);
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