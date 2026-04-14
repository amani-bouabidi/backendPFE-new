package com.ira.formation.services;

import com.ira.formation.dto.DocumentDTO;
import com.ira.formation.entities.Document;
import com.ira.formation.entities.Module;
import com.ira.formation.entities.Utilisateur;
import com.ira.formation.repositories.DocumentRepository;
import com.ira.formation.repositories.InscriptionRepository;
import com.ira.formation.repositories.ModuleRepository;
import com.ira.formation.repositories.UtilisateurRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final ModuleRepository moduleRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final InscriptionRepository inscriptionRepository;

    private final String uploadDir = System.getProperty("user.dir") + "/uploads/documents/";

    // =================== UPLOAD ===================
    @PreAuthorize("hasRole('ADMIN')")
    public DocumentDTO uploadDocument(Long moduleId, MultipartFile file) throws IOException {

        String contentType = file.getContentType();

        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new RuntimeException("Seuls les fichiers PDF sont autorisés");
        }

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module non trouvé"));

        // 📁 créer dossier si non موجود
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 🆔 filename unique
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        File destination = new File(uploadDir + fileName);

        file.transferTo(destination);

        Document document = Document.builder()
                .nom(fileName)
                .filePath("uploads/documents/" + fileName)
                .module(module)
                .build();

        Document saved = documentRepository.save(document);

        return DocumentDTO.builder()
                .id(saved.getId())
                .nom(saved.getNom())
                .filePath(saved.getFilePath())
                .moduleId(moduleId)
                .build();
    }

    // =================== DELETE ===================
    @PreAuthorize("hasAnyRole('ADMIN','FORMATEUR')")
    public void deleteDocument(Long id, String email) {

        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document non trouvé"));

        Utilisateur user = getUser(email);

        // 🔒 check ownership
        if ("FORMATEUR".equals(user.getRole().getNom())) {
            if (!doc.getModule().getFormation().getFormateur().getEmail().equals(email)) {
                throw new RuntimeException("Accès refusé : ce contenu ne vous appartient pas");
            }
        }

        // 🗑️ delete file
        File file = new File(System.getProperty("user.dir") + "/" + doc.getFilePath());
        if (file.exists()) {
            file.delete();
        }

        documentRepository.delete(doc);
    }

    // =================== DOWNLOAD ===================
    @PreAuthorize("hasAnyRole('ADMIN','FORMATEUR','APPRENANT')")
    public ResponseEntity<Resource> downloadDocument(Long id, String email) throws Exception {

        // 1. document
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document non trouvé"));

        // 2. user
        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // 3. role
        String role = user.getRole().getNom();

        // 4. CHECK inscription (CORRIGÉ)
        if ("APPRENANT".equalsIgnoreCase(role)) {

            Long apprenantId = user.getId();
            Long formationId = doc.getModule().getFormation().getId();

            boolean inscrit = inscriptionRepository
                    .existsByApprenantIdAndFormationId(apprenantId, formationId);

            if (!inscrit) {
                throw new RuntimeException("Accès refusé : vous n'êtes pas inscrit");
            }
        }

        // 5. file path
        File file = new File(doc.getFilePath());

        if (!file.exists()) {
            throw new RuntimeException("Fichier introuvable");
        }

        Resource resource = new UrlResource(file.toURI());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + doc.getNom() + "\"")
                .body(resource);
    }
    // =================== HELPER ===================
    private Utilisateur getUser(String email) {
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }
}