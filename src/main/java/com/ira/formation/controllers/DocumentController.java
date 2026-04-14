package com.ira.formation.controllers;

import com.ira.formation.dto.ApiResponse;
import com.ira.formation.dto.DocumentDTO;
import com.ira.formation.services.DocumentService;

import lombok.RequiredArgsConstructor;

import java.security.Principal;

import org.springframework.core.io.Resource; // ✅ الصحيح
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    // =================== UPLOAD ===================
    @PostMapping("/upload/{moduleId}")
    public DocumentDTO uploadDocument(
            @PathVariable Long moduleId,
            @RequestParam("file") MultipartFile file) throws Exception {

        return documentService.uploadDocument(moduleId, file);
    }

    // =================== DELETE ===================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FORMATEUR')")
    public ApiResponse<Void> deleteDocument(@PathVariable Long id, Principal principal) {

        documentService.deleteDocument(id, principal.getName());

        return ApiResponse.success(null, "Document supprimé avec succès");
    }

    // =================== DOWNLOAD ===================
    @GetMapping("/download/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FORMATEUR','APPRENANT')")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id,
                                                     Principal principal) throws Exception {

        return documentService.downloadDocument(id, principal.getName());
    }
}