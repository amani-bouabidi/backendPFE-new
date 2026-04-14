package com.ira.formation.controllers;

import com.ira.formation.dto.FavoriteResponseDTO;
import com.ira.formation.entities.FavoriteType;
import com.ira.formation.services.FavoriteService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    // =================== TOGGLE FAVORITE ❤️ ===================
    @PostMapping("/toggle")
    public String toggleFavorite(
            @RequestParam Long elementId,
            @RequestParam FavoriteType type,
            Authentication authentication
    ) {
        String email = authentication.getName();
        return favoriteService.toggleFavorite(email, elementId, type);
    }

    // =================== GET MY FAVORITES ===================
    @GetMapping("/my")
    public List<FavoriteResponseDTO> getMyFavorites(Authentication authentication) {

        String email = authentication.getName();
        return favoriteService.getMyFavorites(email);
    }

    // =================== CHECK FAVORITE ❤️ ===================
    @PreAuthorize("hasAnyRole('ADMIN','FORMATEUR','APPRENANT')")
    @GetMapping("/check")
    public boolean checkFavorite(
            @RequestParam Long elementId,
            @RequestParam FavoriteType type,
            Authentication authentication
    ) {
        String email = authentication.getName();
        return favoriteService.isFavorite(email, elementId, type);
    }
}