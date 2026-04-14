package com.ira.formation.services;

import com.ira.formation.dto.AttestationDTO;
import com.ira.formation.dto.ProgressDTO;
import com.ira.formation.entities.*;
import com.ira.formation.repositories.*;

import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttestationService {

    private final AttestationRepository attestationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final FormationRepository formationRepository;
    private final ProgressService progressService;

    // ================= GENERATE (ADMIN ONLY) =================
    public AttestationDTO generateAttestation(Long apprenantId, Long formationId) throws Exception {

        Utilisateur user = utilisateurRepository.findById(apprenantId)
                .orElseThrow(() -> new RuntimeException("Apprenant non trouvé"));

        Formation formation = formationRepository.findById(formationId)
                .orElseThrow(() -> new RuntimeException("Formation non trouvée"));

        // 🔐 check progress
        ProgressDTO progress = progressService.getMyProgress(user.getEmail(), formationId);

        if (!progress.isCompleted()) {
            throw new RuntimeException("Formation non complétée");
        }

        // 🔐 avoid duplicate
        Attestation existing = attestationRepository
                .findByApprenantAndFormation(user, formation)
                .orElse(null);

        if (existing != null) {
            return mapToDTO(existing);
        }

        // 📁 file
        String folder = System.getProperty("user.dir") + "/attestations/";
        new File(folder).mkdirs();

        String fileName = "attestation_" + user.getId() + "_" + formationId + ".pdf";
        String filePath = "attestations/" + fileName;
        String fullPath = System.getProperty("user.dir") + "/" + filePath;

        // 📄 PDF
        PdfWriter writer = new PdfWriter(fullPath);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("ATTESTATION DE PARTICIPATION"));
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Nom: " + user.getNom() + " " + user.getPrenom()));
        document.add(new Paragraph("Formation: " + formation.getTitre()));
        document.add(new Paragraph("Date: " + LocalDateTime.now()));

        document.close();

        // 💾 save
        Attestation att = Attestation.builder()
                .apprenant(user)
                .formation(formation)
                .filePath(filePath)
                .createdAt(LocalDateTime.now())
                .build();

        return mapToDTO(attestationRepository.save(att));
    }

    // ================= MY ATTESTATIONS =================
    public List<AttestationDTO> getMyAttestations(String email) {

        return attestationRepository.findAll().stream()
                .filter(a -> a.getApprenant().getEmail().equals(email))
                .map(this::mapToDTO)
                .toList();
    }

    // ================= ADMIN ALL =================
    public List<AttestationDTO> getAllAttestations() {

        return attestationRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    // ================= MAPPER =================
    private AttestationDTO mapToDTO(Attestation att) {

        return AttestationDTO.builder()
                .id(att.getId())
                .filePath(att.getFilePath())
                .apprenantNom(att.getApprenant().getNom() + " " + att.getApprenant().getPrenom())
                .formationTitre(att.getFormation().getTitre())
                .createdAt(att.getCreatedAt().toString())
                .build();
    }
}