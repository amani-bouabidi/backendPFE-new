package com.ira.formation.controllers;
import com.ira.formation.dto.AuthResponseDTO;
import com.ira.formation.dto.RegisterRequest;
import com.ira.formation.dto.LoginRequest;
import com.ira.formation.entities.RefreshToken;
import com.ira.formation.entities.Utilisateur;
import com.ira.formation.entities.Role;
import com.ira.formation.repositories.UtilisateurRepository;
import com.ira.formation.security.JwtService;
import com.ira.formation.services.RefreshTokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import com.ira.formation.repositories.RoleRepository;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")  // ✅
public class AuthController {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final RoleRepository roleRepository;

    public AuthController(UtilisateurRepository utilisateurRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService,
                          RefreshTokenService refreshTokenService,
                          RoleRepository roleRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.roleRepository = roleRepository;
    }
  

    // ----------------- LOGIN -----------------
    @PostMapping("/login")
    public AuthResponseDTO login(@RequestBody LoginRequest request) {

        Optional<Utilisateur> utilisateurOpt =
                utilisateurRepository.findByEmail(request.getEmail());

        if (utilisateurOpt.isEmpty()) {
            throw new RuntimeException("Email incorrect ❌");
        }

        Utilisateur utilisateur = utilisateurOpt.get();

        if (!passwordEncoder.matches(request.getPassword(), utilisateur.getPassword())) {
            throw new RuntimeException("Mot de passe incorrect ❌");
        }

        // Générer JWT et Refresh Token
        String accessToken = jwtService.generateToken(utilisateur.getEmail());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(utilisateur);

        // ⚡ Ici on envoie le role pur
        return new AuthResponseDTO(
                accessToken,
                refreshToken.getToken(),
                utilisateur.getRole().getNom() // <-- "ADMIN" ou "FORMATEUR" ou "APPRENANT"
        );
    }

    // ----------------- REFRESH TOKEN -----------------
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refresh(@RequestBody String refreshTokenString) {
        RefreshToken refreshToken = refreshTokenService
                .getRefreshTokenByToken(refreshTokenString)
                .orElseThrow(() -> new RuntimeException("Refresh token invalide ❌"));

        if (!refreshTokenService.isValid(refreshToken)) {
            throw new RuntimeException("Refresh token expiré ou révoqué ❌");
        }

        String newAccessToken = jwtService.generateToken(refreshToken.getUtilisateur().getEmail());
        return ResponseEntity.ok(new AuthResponseDTO("Token renouvelé ✅", newAccessToken, refreshTokenString));
    }

    // ----------------- LOGOUT -----------------
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestParam String refreshTokenString) {
        RefreshToken refreshToken = refreshTokenService
                .getRefreshTokenByToken(refreshTokenString)
                .orElseThrow(() -> new RuntimeException("Refresh token invalide ❌"));

        refreshTokenService.revokeToken(refreshToken);
        return ResponseEntity.ok("Déconnexion réussie ✅");
    }
    
    
 // ----------------- REGISTER -----------------
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {

        if (utilisateurRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email déjà utilisé !");
        }

        // Chercher le rôle APPRENANT dans la DB
        Role role = roleRepository.findByNom("APPRENANT")
                .orElseThrow(() -> new RuntimeException("Rôle APPRENANT introuvable !"));

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(request.getNom());
        utilisateur.setPrenom(request.getPrenom());  
        utilisateur.setEmail(request.getEmail());
        utilisateur.setPassword(passwordEncoder.encode(request.getPassword()));
        utilisateur.setRole(role);  // <-- ici on met Role depuis DB

        utilisateurRepository.save(utilisateur);

        return ResponseEntity.ok("Compte créé avec succès !");
    }
}