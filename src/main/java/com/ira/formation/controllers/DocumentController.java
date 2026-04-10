package com.ira.formation.controllers;

import com.ira.formation.dto.DocumentDTO;
import com.ira.formation.services.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload/{moduleId}")
    public DocumentDTO uploadDocument(
            @PathVariable Long moduleId,
            @RequestParam("file") MultipartFile file) throws Exception {

        return documentService.uploadDocument(moduleId, file);
    }
}