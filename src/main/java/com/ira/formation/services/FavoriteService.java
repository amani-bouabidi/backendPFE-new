package com.ira.formation.services;

import com.ira.formation.dto.DocumentDTO;
import com.ira.formation.dto.FavoriteResponseDTO;
import com.ira.formation.dto.ModuleDTO;
import com.ira.formation.dto.VideoDTO;
import com.ira.formation.entities.*;
import com.ira.formation.entities.Module;
import com.ira.formation.repositories.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ModuleRepository moduleRepository;
    private final VideoRepository videoRepository;
    private final DocumentRepository documentRepository;
    private final InscriptionRepository inscriptionRepository;

    // =================== MAIN TOGGLE ===================
    public String toggleFavorite(String email, Long elementId, FavoriteType type) {

        Utilisateur user = getUser(email);

        // 🔐 check access
        checkAccess(user, elementId, type);

        Favorite existing = favoriteRepository
                .findByUserAndElementIdAndType(user, elementId, type)
                .orElse(null);

        if (existing != null) {
            favoriteRepository.delete(existing);
            return "removed";
        }

        Favorite fav = new Favorite();
        fav.setUser(user);
        fav.setElementId(elementId);
        fav.setType(type);

        favoriteRepository.save(fav);

        return "added";
    }

    // =================== CHECK FAVORITE ===================
    public boolean isFavorite(String email, Long elementId, FavoriteType type) {

        Utilisateur user = getUser(email);

        // 🔐 check access (نفس logic متاع toggle)
        checkAccess(user, elementId, type);

        // ✔️ إذا وصل لهنا → العنصر موجود و عندو access
        return favoriteRepository.existsByUserAndElementIdAndType(user, elementId, type);
    }

 // =================== GET FAVORITES ===================
    public List<FavoriteResponseDTO> getMyFavorites(String email) {

        Utilisateur user = getUser(email);

        List<Favorite> favorites = favoriteRepository.findByUser(user);

        return favorites.stream().map(fav -> {

            Object content = null;

            switch (fav.getType()) {

            case MODULE -> {
                Module m = moduleRepository.findById(fav.getElementId()).orElse(null);

                if (m != null) {

                    List<DocumentDTO> documents = m.getDocuments()
                            .stream()
                            .map(d -> new DocumentDTO(
                                    d.getId(),
                                    d.getNom(),
                                    d.getFilePath(),
                                    m.getId()
                            ))
                            .toList();

                    List<VideoDTO> videos = m.getVideos()
                            .stream()
                            .map(v -> new VideoDTO(
                                    v.getId(),
                                    v.getTitre(),
                                    v.getFilePath(),
                                    m.getId()
                            ))
                            .toList();

                    content = ModuleDTO.builder()
                            .id(m.getId())
                            .titre(m.getTitre())
                            .description(m.getDescription())
                            .documents(documents)
                            .videos(videos)
                            .build();
                }
            }

                case VIDEO -> {
                    Video v = videoRepository.findById(fav.getElementId()).orElse(null);

                    if (v != null) {
                        content = new VideoDTO(
                                v.getId(),
                                v.getTitre(),
                                v.getFilePath(),
                                v.getModule().getId()
                        );
                    }
                }

                case DOCUMENT -> {
                    Document d = documentRepository.findById(fav.getElementId()).orElse(null);

                    if (d != null) {
                        content = new DocumentDTO(
                                d.getId(),
                                d.getNom(),
                                d.getFilePath(),
                                d.getModule().getId()
                        );
                    }
                }
            }

            return new FavoriteResponseDTO(
                    fav.getElementId(),
                    fav.getType().name(),
                    content
            );

        }).toList();
    }

    // =================== SECURITY CHECK ===================
    private void checkAccess(Utilisateur user, Long elementId, FavoriteType type) {

        if (!"APPRENANT".equals(user.getRole().getNom())) {
            return; // ADMIN / FORMATEUR OK
        }

        Formation formation = null;

        switch (type) {

            case MODULE -> {
                Module module = moduleRepository.findById(elementId)
                        .orElseThrow(() -> new RuntimeException("Module not found"));
                formation = module.getFormation();
            }

            case VIDEO -> {
                Video video = videoRepository.findById(elementId)
                        .orElseThrow(() -> new RuntimeException("Video not found"));
                formation = video.getModule().getFormation();
            }

            case DOCUMENT -> {
                Document doc = documentRepository.findById(elementId)
                        .orElseThrow(() -> new RuntimeException("Document not found"));
                formation = doc.getModule().getFormation();
            }
        }

        boolean inscrit = inscriptionRepository
                .existsByApprenantAndFormation(user, formation);

        if (!inscrit) {
            throw new RuntimeException("Accès refusé : vous n'êtes pas inscrit");
        }
    }

    // =================== USER HELPER ===================
    private Utilisateur getUser(String email) {
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}