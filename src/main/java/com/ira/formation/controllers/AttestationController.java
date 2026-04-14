package com.ira.formation.controllers;

import com.ira.formation.dto.AttestationDTO;
import com.ira.formation.services.AttestationService;

import lombok.RequiredArgsConstructor;

import org.springframework.core.io.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/api/attestations")
@RequiredArgsConstructor
public class AttestationController {

    private final AttestationService attestationService;

    // ================= GENERATE (ADMIN ONLY) =================
    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN')")
    public AttestationDTO generate(
            @RequestParam Long apprenantId,
            @RequestParam Long formationId
    ) throws Exception {

        return attestationService.generateAttestation(apprenantId, formationId);
    }

    // ================= MY ATTESTATIONS =================
    @GetMapping("/my")
    @PreAuthorize("hasRole('APPRENANT')")
    public List<AttestationDTO> myAttestations(Authentication auth) {

        return attestationService.getMyAttestations(auth.getName());
    }

    // ================= ADMIN ALL =================
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AttestationDTO> all() {

        return attestationService.getAllAttestations();
    }

    // ================= DOWNLOAD SECURE =================
    @GetMapping("/download/{id}")
    @PreAuthorize("hasAnyRole('APPRENANT','ADMIN')")
    public ResponseEntity<Resource> download(
            @PathVariable Long id,
            Authentication auth
    ) throws Exception {

        var att = attestationService.getAllAttestations().stream()
                .filter(a -> a.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Attestation not found"));

        // file path
        File file = new File(System.getProperty("user.dir") + "/" + att.getFilePath());
        Resource resource = new UrlResource(file.toURI());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=attestation.pdf")
                .body(resource);
    }
}