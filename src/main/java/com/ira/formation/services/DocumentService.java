package com.ira.formation.services;

import com.ira.formation.dto.DocumentDTO;
import com.ira.formation.entities.Document;
import com.ira.formation.entities.Module;
import com.ira.formation.repositories.DocumentRepository;
import com.ira.formation.repositories.ModuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final ModuleRepository moduleRepository;

    private final String uploadDir = System.getProperty("user.dir") + "/uploads/documents/";

    @PreAuthorize("hasRole('ADMIN')")
    public DocumentDTO uploadDocument(Long moduleId, MultipartFile file) throws IOException {

        String contentType = file.getContentType();

        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new RuntimeException("Seuls les fichiers PDF sont autorisés");
        }

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module non trouvé"));

        // ✅ créer dossier si non موجود
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // ✅ éviter duplicate names
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        File destination = new File(uploadDir + fileName);

        file.transferTo(destination);

        Document document = Document.builder()
                .nom(fileName)
                .filePath("/documents/" + fileName) // ✅ مهم
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
}