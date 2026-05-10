package com.ira.formation.controllers;

import com.ira.formation.dto.AttestationDTO;
import com.ira.formation.services.AttestationService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<byte[]> download(
            @PathVariable Long id,
            Authentication auth
    ) throws Exception {

        // ✅ PDF regénéré à la volée depuis les données en BD
        // Le design est TOUJOURS garanti, indépendamment du disque
        byte[] pdfBytes = attestationService.generatePdfBytes(id);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=attestation.pdf")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(pdfBytes.length))
                .body(pdfBytes);
    }
}