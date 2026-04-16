package com.ira.formation.controllers;

import com.ira.formation.entities.Video;
import com.ira.formation.entities.Utilisateur;
import com.ira.formation.repositories.VideoRepository;
import com.ira.formation.repositories.UtilisateurRepository;
import com.ira.formation.repositories.InscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoStreamingController {

    private final VideoRepository videoRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final InscriptionRepository inscriptionRepository;

    private final String uploadDir = "uploads/videos/";

    @GetMapping("/stream/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FORMATEUR','APPRENANT')")
    public ResponseEntity<Resource> streamVideo(
            @PathVariable Long id,
            @RequestHeader HttpHeaders headers,
            Authentication auth) throws IOException {

        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vidéo non trouvée"));

        Utilisateur user = utilisateurRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // 🔐 SECURITY MÉTIER

        // ADMIN → accès libre
        if (!"ADMIN".equals(user.getRole().getNom())) {

            // FORMATEUR → فقط vidéos متاع formations متاعو
            if ("FORMATEUR".equals(user.getRole().getNom())) {

                if (!video.getModule().getFormation().getFormateur()
                        .getEmail().equals(user.getEmail())) {

                    throw new RuntimeException("Accès refusé (pas votre formation)");
                }
            }

            // APPRENANT → لازم يكون inscrit
            if ("APPRENANT".equals(user.getRole().getNom())) {

                boolean inscrit = inscriptionRepository.existsByApprenantAndFormation(
                        user,
                        video.getModule().getFormation()
                );

                if (!inscrit) {
                    throw new RuntimeException("Accès refusé (non inscrit)");
                }
            }
        }

        // ================= STREAM =================

        Path path = Paths.get(uploadDir + video.getTitre());
        Resource resource = new UrlResource(path.toUri());

        long fileLength = resource.contentLength();

        HttpRange range = headers.getRange().isEmpty()
                ? null
                : headers.getRange().get(0);

        if (range == null) {
            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf("video/mp4"))
                    .contentLength(fileLength)
                    .body(resource);
        }

        long start = range.getRangeStart(fileLength);
        long end = range.getRangeEnd(fileLength);

        long rangeLength = end - start + 1;

        Resource region = new UrlResource(path.toUri());

        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .contentType(MediaType.valueOf("video/mp4"))
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .header(HttpHeaders.CONTENT_RANGE,
                        "bytes " + start + "-" + end + "/" + fileLength)
                .contentLength(rangeLength)
                .body(region);
    }
}