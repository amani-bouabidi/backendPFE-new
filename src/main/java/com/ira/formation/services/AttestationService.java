package com.ira.formation.services;

import com.ira.formation.dto.AttestationDTO;
import com.ira.formation.dto.ProgressDTO;
import com.ira.formation.entities.*;
import com.ira.formation.repositories.*;

import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.io.font.constants.StandardFonts;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AttestationService {

    private final AttestationRepository attestationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final FormationRepository formationRepository;
    private final ProgressService progressService;
    private final NotificationService notificationService;

    // Colors matching the Angular design
    private static final DeviceRgb GREEN  = new DeviceRgb(0x80, 0xC1, 0x97);  // #80c197
    private static final DeviceRgb YELLOW = new DeviceRgb(0xFF, 0xF4, 0x68);  // #fff468
    private static final DeviceRgb BLUE   = new DeviceRgb(0x73, 0xD1, 0xF7);  // #73d1f7
    private static final DeviceRgb DARK   = new DeviceRgb(0x2D, 0x37, 0x48);  // #2d3748
    private static final DeviceRgb GRAY   = new DeviceRgb(0x71, 0x80, 0x96);  // #718096
    private static final DeviceRgb BLUE2  = new DeviceRgb(0x29, 0x80, 0xB9);  // #2980b9
    private static final DeviceRgb BGGREEN= new DeviceRgb(0xF7, 0xFA, 0xF7);  // #f7faf7

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

        // 💾 On ne stocke plus sur le disque — on sauvegarde uniquement les métadonnées en BD
        // Le PDF est regénéré à la volée à chaque téléchargement (design toujours garanti)
        Attestation att = Attestation.builder()
                .apprenant(user)
                .formation(formation)
                .filePath("") // plus utilisé
                .createdAt(LocalDateTime.now())
                .build();

        Attestation saved = attestationRepository.save(att);

        // ✅ Notifier l'apprenant
        String message = String.format(
            "🎓 Félicitations %s ! Votre attestation de participation pour la formation « %s » est disponible. Consultez-la dans votre espace Attestations.",
            user.getPrenom(),
            formation.getTitre()
        );
        notificationService.createNotification(user, message);

        return mapToDTO(saved);
    }

    // ================= GENERATE PDF EN MÉMOIRE (appelé à chaque download) =================
    public byte[] generatePdfBytes(Long attestationId) throws Exception {

        Attestation att = attestationRepository.findById(attestationId)
                .orElseThrow(() -> new RuntimeException("Attestation non trouvée"));

        return buildPdfBytes(att.getApprenant(), att.getFormation(), att.getCreatedAt());
    }

    private byte[] buildPdfBytes(Utilisateur user, Formation formation, LocalDateTime createdAt) throws Exception {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        document.setMargins(0, 0, 0, 0);

        PdfFont fontBold   = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont fontNormal = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont fontItalic = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);

        // ── Bande haut (vert 2/4 + jaune 1/4 + bleu 1/4) ──────────────
        Table topBar = new Table(new float[]{2, 1, 2}).setWidth(UnitValue.createPercentValue(100));
        topBar.addCell(new Cell().setHeight(12).setBackgroundColor(GREEN).setBorder(Border.NO_BORDER));
        topBar.addCell(new Cell().setHeight(12).setBackgroundColor(YELLOW).setBorder(Border.NO_BORDER));
        topBar.addCell(new Cell().setHeight(12).setBackgroundColor(BLUE).setBorder(Border.NO_BORDER));
        document.add(topBar);

        // ── Contenu avec marges ─────────────────────────────────────────
        document.setMargins(36, 50, 36, 50);

        // ── En-tête institution ──────────────────────────────────────────
        Table header = new Table(new float[]{60, 1}).setWidth(UnitValue.createPercentValue(100));
        Cell instCell = new Cell().setBorder(Border.NO_BORDER).setPaddingTop(20).setPaddingBottom(10);
        Paragraph instName = new Paragraph("Institut des Régions Arides")
                .setFont(fontBold).setFontSize(15).setFontColor(DARK);
        Paragraph instSub = new Paragraph("Médenine — Tunisie")
                .setFont(fontNormal).setFontSize(11).setFontColor(GRAY);
        instCell.add(instName).add(instSub);
        header.addCell(instCell);
        document.add(header);

        // ── Divider ──────────────────────────────────────────────────────
        LineSeparator ls = new LineSeparator(new SolidLine(1f));
        ls.setWidth(UnitValue.createPercentValue(100));
        ls.setStrokeColor(GREEN);
        document.add(ls);
        document.add(new Paragraph(" ").setFontSize(6));

        // ── Titre attestation ────────────────────────────────────────────
        document.add(new Paragraph("CERTIFICAT DE RÉUSSITE")
                .setFont(fontBold).setFontSize(9).setFontColor(GRAY)
                .setCharacterSpacing(3)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(18));

        document.add(new Paragraph("Attestation de Participation")
                .setFont(fontBold).setFontSize(26).setFontColor(DARK)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(0));

        document.add(new Paragraph(" ").setFontSize(10));

        // ── Corps ────────────────────────────────────────────────────────
        document.add(new Paragraph("Cette attestation est décernée à")
                .setFont(fontNormal).setFontSize(12).setFontColor(GRAY)
                .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph(user.getPrenom() + " " + user.getNom())
                .setFont(fontBold).setFontSize(28).setFontColor(DARK)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(4).setMarginBottom(4));

        document.add(new Paragraph("pour avoir suivi avec succès la formation")
                .setFont(fontNormal).setFontSize(12).setFontColor(GRAY)
                .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph("« " + formation.getTitre() + " »")
                .setFont(fontItalic).setFontSize(16).setFontColor(BLUE2)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(6).setMarginBottom(20));

        // ── Pied ─────────────────────────────────────────────────────────
        document.add(new Paragraph(" ").setFontSize(20));

        String dateStr = (createdAt != null ? createdAt : LocalDateTime.now())
                .format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH));

        Table footer = new Table(new float[]{1, 1})
                .setWidth(UnitValue.createPercentValue(100))
                .setBackgroundColor(BGGREEN)
                .setBorderTop(new com.itextpdf.layout.borders.SolidBorder(GREEN, 1));

        Cell dateCell = new Cell().setBorder(Border.NO_BORDER).setPadding(12);
        dateCell.add(new Paragraph(dateStr)
                .setFont(fontNormal).setFontSize(11).setFontColor(GRAY));
        footer.addCell(dateCell);

        Cell sealCell = new Cell().setBorder(Border.NO_BORDER).setPadding(12)
                .setTextAlignment(TextAlignment.RIGHT);
        sealCell.add(new Paragraph("IRA — Médenine")
                .setFont(fontBold).setFontSize(11).setFontColor(GREEN));
        footer.addCell(sealCell);

        document.add(footer);

        // ── Bande bas inversée ───────────────────────────────────────────
        document.setMargins(0, 0, 0, 0);
        Table botBar = new Table(new float[]{2, 1, 2}).setWidth(UnitValue.createPercentValue(100));
        botBar.addCell(new Cell().setHeight(12).setBackgroundColor(BLUE).setBorder(Border.NO_BORDER));
        botBar.addCell(new Cell().setHeight(12).setBackgroundColor(YELLOW).setBorder(Border.NO_BORDER));
        botBar.addCell(new Cell().setHeight(12).setBackgroundColor(GREEN).setBorder(Border.NO_BORDER));
        document.add(botBar);

        document.close();

        return baos.toByteArray();
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