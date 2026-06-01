package com.privacyshield.privacy_shield.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
public class FileParserService {

    // ========================================
    // PDF se text extract karo
    // ========================================
    public String extractFromPdf(MultipartFile file) throws IOException {

        byte[] fileBytes = file.getBytes();

        // Apache PDFBox 3.x ke liye Loader.loadPDF use kiya hai
        try (PDDocument document = Loader.loadPDF(fileBytes)) {

            PDFTextStripper stripper = new PDFTextStripper();

            // Saare pages ka text ek saath nikalo
            String text = stripper.getText(document);

            System.out.println("PDF Pages: " + document.getNumberOfPages());
            System.out.println("Extracted text length: " + text.length());

            return text;
        }
    }

    // ========================================
    // DOCX se text extract karo
    // ========================================
    public String extractFromDocx(MultipartFile file) throws IOException {

        try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {

            StringBuilder text = new StringBuilder();

            // Saare paragraphs padho
            for (XWPFParagraph para : document.getParagraphs()) {
                text.append(para.getText()).append("\n");
            }

            // Tables ka text bhi nikalo
            for (XWPFTable table : document.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        text.append(cell.getText()).append(" | ");
                    }
                    text.append("\n");
                }
            }

            return text.toString();
        }
    }

    // ========================================
    // File type check karo
    // ========================================
    public String getFileType(MultipartFile file) {
        String name = file.getOriginalFilename();
        if (name == null) return "UNKNOWN";

        if (name.toLowerCase().endsWith(".pdf"))
            return "PDF";
        if (name.toLowerCase().endsWith(".docx"))
            return "DOCX";

        return "UNSUPPORTED";
    }
}