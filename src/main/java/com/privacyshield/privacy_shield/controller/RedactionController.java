package com.privacyshield.privacy_shield.controller;

import com.privacyshield.privacy_shield.dto.RedactionResult;
import com.privacyshield.privacy_shield.service.RedactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/redact")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RedactionController {

    private final RedactionService redactionService;

    // ========================================
    // POST /api/redact/process
    // File upload karo → redacted result pao
    // ========================================
    @PostMapping("/process")
    public ResponseEntity<RedactionResult> processFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("email") String email) {

        try {
            RedactionResult result =
                    redactionService.processFile(file, email);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new RedactionResult(
                            "", 0, null, false,
                            "Error: " + e.getMessage()
                    ));
        }
    }

    // ========================================
    // GET /api/redact/test
    // Simple text test karo — file bina
    // ========================================
    @PostMapping("/test-text")
    public ResponseEntity<RedactionResult> testText(
            @RequestBody String text) {

        try {
            RedactionResult result =
                    redactionService
                            .getPiiDetectorService()
                            .detectAndRedact(text);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Inject karo
    private final PdfGeneratorService pdfGeneratorService;

    // ========================================
// POST /api/redact/download
// Redacted PDF file download karo
// ========================================
    @PostMapping("/download")
    public ResponseEntity<byte[]> downloadRedacted(
            @RequestParam("file") MultipartFile file,
            @RequestParam("email") String email)
            throws Exception {

        // 1. File process karo
        RedactionResult result =
                redactionService.processFile(file, email);

        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().build();
        }

        // 2. Redacted PDF generate karo
        byte[] pdfBytes =
                pdfGeneratorService.generateRedactedPdf(
                        result.getRedactedText(),
                        file.getOriginalFilename());

        // 3. Download response return karo
        String filename = "REDACTED_"
                + file.getOriginalFilename()
                .replace(".docx", ".pdf");

        return ResponseEntity.ok()
                .header("Content-Disposition",
                        "attachment; filename=\"" + filename + "\"")
                .header("Content-Type",
                        "application/pdf")
                .body(pdfBytes);
    }
}
