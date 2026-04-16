package com.ira.formation.services;

import com.ira.formation.dto.VideoDTO;
import com.ira.formation.entities.Module;
import com.ira.formation.entities.Utilisateur;
import com.ira.formation.entities.Video;
import com.ira.formation.repositories.ModuleRepository;
import com.ira.formation.repositories.UtilisateurRepository;
import com.ira.formation.repositories.VideoRepository;
import com.ira.formation.repositories.InscriptionRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoRepository videoRepository;
    private final ModuleRepository moduleRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final InscriptionRepository inscriptionRepository;

    private final String uploadDir = System.getProperty("user.dir") + "/uploads/videos/";

    // =================== UPLOAD ===================
    @PreAuthorize("hasRole('ADMIN')")
    public VideoDTO uploadVideo(Long moduleId, MultipartFile file) throws IOException {

        String contentType = file.getContentType();

        if (contentType == null || !contentType.equals("video/mp4")) {
            throw new RuntimeException("Seuls les fichiers MP4 sont autorisés");
        }

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module non trouvé"));

        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        File destination = new File(uploadDir + fileName);

        file.transferTo(destination);

        Video video = Video.builder()
                .titre(fileName)
                .filePath("uploads/videos/" + fileName)
                .module(module)
                .build();

        Video saved = videoRepository.save(video);

        return VideoDTO.builder()
                .id(saved.getId())
                .titre(saved.getTitre())
                .filePath(saved.getFilePath())
                .moduleId(moduleId)
                .build();
    }

    // =================== DELETE ===================
    @PreAuthorize("hasAnyRole('ADMIN','FORMATEUR')")
    public void deleteVideo(Long id, String email) {

        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video non trouvé"));

        Utilisateur user = getUser(email);

        if ("FORMATEUR".equals(user.getRole().getNom())) {
            if (!video.getModule().getFormation().getFormateur().getEmail().equals(email)) {
                throw new RuntimeException("Accès refusé : ce contenu ne vous appartient pas");
            }
        }

        File file = new File(System.getProperty("user.dir") + "/" + video.getFilePath());
        if (file.exists()) {
            file.delete();
        }

        videoRepository.delete(video);
    }

 // =================== DOWNLOAD ===================
    
    public ResponseEntity<Resource> downloadVideo(Long id, String email) throws Exception {

        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video non trouvé"));

        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        String role = user.getRole().getNom();

        // 🔐 ADMIN → accès libre

        if (!"ADMIN".equals(role)) {

            // 🔐 FORMATEUR → فقط formation متاعو
            if ("FORMATEUR".equals(role)) {
                if (!video.getModule().getFormation().getFormateur()
                        .getEmail().equals(email)) {

                    throw new RuntimeException("Accès refusé : ce contenu ne vous appartient pas");
                }
            }

            // 🔐 APPRENANT → لازم يكون inscrit
            if ("APPRENANT".equals(role)) {

                boolean inscrit = inscriptionRepository
                        .existsByApprenantAndFormation(
                                user,
                                video.getModule().getFormation()
                        );

                if (!inscrit) {
                    throw new RuntimeException("Accès refusé : vous n'êtes pas inscrit");
                }
            }
        }

        // ================= FILE =================

        File file = new File(System.getProperty("user.dir") + "/" + video.getFilePath());

        if (!file.exists()) {
            throw new RuntimeException("Fichier introuvable");
        }

        Resource resource = new UrlResource(file.toURI());

        // 🔥 detect video type
        String contentType = "application/octet-stream";

        if (video.getFilePath().endsWith(".mp4")) {
            contentType = "video/mp4";
        } else if (video.getFilePath().endsWith(".avi")) {
            contentType = "video/x-msvideo";
        } else if (video.getFilePath().endsWith(".mov")) {
            contentType = "video/quicktime";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + video.getTitre() + "\"")
                .body(resource);
    }
    // =================== HELPER ===================
    private Utilisateur getUser(String email) {
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }
}