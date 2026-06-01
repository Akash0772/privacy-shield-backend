package com.privacyshield.privacy_shield.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.privacyshield.privacy_shield.dto.RedactionResult;
import com.privacyshield.privacy_shield.entity.AuditLog;
import com.privacyshield.privacy_shield.entity.User;
import com.privacyshield.privacy_shield.repository.AuditLogRepository;
import com.privacyshield.privacy_shield.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class RedactionService {

    private final FileParserService fileParserService;
    private final PiiDetectorService piiDetectorService;
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    // Free plan limit
    private static final int FREE_LIMIT = 10;

    // ========================================
    // MAIN METHOD — File process karo
    // ========================================
    public RedactionResult processFile(
            MultipartFile file,
            String userEmail) throws Exception {

        // 1. User dhundho
        User user = userRepository
                .findByEmail(userEmail)
                .orElseThrow(() ->
                        new RuntimeException("User nahi mila"));

        // 2. Free plan limit check karo
        if ("FREE".equals(user.getPlan())
                && user.getDocumentsUsed() >= FREE_LIMIT) {
            return new RedactionResult(
                    "", 0, null, false,
                    "Free plan limit (10 docs) khatam! " +
                            "Pro plan lo — ₹999/month"
            );
        }

        // 3. File type detect karo
        String fileType =
                fileParserService.getFileType(file);

        if ("UNSUPPORTED".equals(fileType)) {
            return new RedactionResult(
                    "", 0, null, false,
                    "Sirf PDF aur DOCX supported hain!"
            );
        }

        // 4. Text extract karo
        String extractedText;
        if ("PDF".equals(fileType)) {
            extractedText =
                    fileParserService.extractFromPdf(file);
        } else {
            extractedText =
                    fileParserService.extractFromDocx(file);
        }

        // 5. PII detect aur redact karo ⭐
        RedactionResult result =
                piiDetectorService.detectAndRedact(
                        extractedText);

        // 6. Document count update karo
        user.setDocumentsUsed(
                user.getDocumentsUsed() + 1);
        userRepository.save(user);

        // 7. Audit log save karo
        saveAuditLog(user, file.getOriginalFilename(),
                fileType, result);

        return result;
    }

    // ========================================
    // Audit log save karo
    // ========================================
    private void saveAuditLog(
            User user,
            String fileName,
            String fileType,
            RedactionResult result) {
        try {
            AuditLog log = new AuditLog();
            log.setUser(user);
            log.setFileName(fileName);
            log.setFileType(fileType);
            log.setPiiCount(result.getTotalPiiFound());
            log.setPiiBreakdown(
                    objectMapper.writeValueAsString(
                            result.getPiiBreakdown()));

            auditLogRepository.save(log);
        } catch (Exception e) {
            System.err.println(
                    "Audit log save nahi hua: " + e.getMessage());
        }
    }

    // Controller ke liye getter
    public PiiDetectorService getPiiDetectorService() {
        return piiDetectorService;
    }
}
