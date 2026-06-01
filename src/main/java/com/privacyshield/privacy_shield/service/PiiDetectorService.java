package com.privacyshield.privacy_shield.service;

import com.privacyshield.privacy_shield.dto.RedactionResult;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.regex.*;

@Service
public class PiiDetectorService {

    // ========================================
    // INDIA-SPECIFIC PII PATTERNS
    // ========================================
    private static final Map<String, String> PII_PATTERNS
            = new LinkedHashMap<>();

    static {
        // PAN Card — ABCDE1234F format
        PII_PATTERNS.put("PAN_CARD",
                "[A-Z]{5}[0-9]{4}[A-Z]{1}");

        // Aadhaar — 3456 7890 1234 format
        PII_PATTERNS.put("AADHAAR",
                "[2-9]{1}[0-9]{3}\\s[0-9]{4}\\s[0-9]{4}");

        // Indian Phone — +91 9876543210 format
        PII_PATTERNS.put("PHONE",
                "(\\+91[\\s-]?)?[6-9][0-9]{9}");

        // Email
        PII_PATTERNS.put("EMAIL",
                "[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+" +
                        "\\.[a-zA-Z]{2,}");

        // GSTIN — 27ABCDE1234F1Z5 format
        PII_PATTERNS.put("GSTIN",
                "[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}" +
                        "[1-9A-Z]{1}Z[0-9A-Z]{1}");

        // IFSC Code — HDFC0001234 format
        PII_PATTERNS.put("IFSC",
                "[A-Z]{4}0[A-Z0-9]{6}");

        // Credit/Debit Card
        PII_PATTERNS.put("CARD_NUMBER",
                "[0-9]{4}[\\s\\-]?[0-9]{4}[\\s\\-]?" +
                        "[0-9]{4}[\\s\\-]?[0-9]{4}");

        // Indian Passport — A1234567 format
        PII_PATTERNS.put("PASSPORT",
                "[A-Z]{1}[0-9]{7}");

        // UPI ID — name@bank format
        PII_PATTERNS.put("UPI_ID",
                "[a-zA-Z0-9.\\-_]{2,}@[a-zA-Z]{2,64}");

        // Bank Account (9-18 digits)
        PII_PATTERNS.put("BANK_ACCOUNT",
                "\\b[0-9]{9,18}\\b");

        // Driving License — MH12 20110012345 format
        PII_PATTERNS.put("DRIVING_LICENSE",
                "[A-Z]{2}[0-9]{2}[\\s]?[0-9]{11}");

        // Vehicle Number — MH12AB1234 format
        PII_PATTERNS.put("VEHICLE_NUMBER",
                "[A-Z]{2}[0-9]{2}[A-Z]{2}[0-9]{4}");
    }

    // ========================================
    // MAIN METHOD — Text scan karo aur redact karo
    // ========================================
    public RedactionResult detectAndRedact(String originalText) {

        if (originalText == null || originalText.isEmpty()) {
            return new RedactionResult(
                    "", 0, new HashMap<>(),
                    false, "Text empty hai"
            );
        }

        String redactedText = originalText;
        Map<String, Integer> breakdown = new LinkedHashMap<>();
        int totalCount = 0;

        // Har pattern ke liye scan karo
        for (Map.Entry<String, String> entry
                : PII_PATTERNS.entrySet()) {

            String piiType = entry.getKey();
            String pattern = entry.getValue();

            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(redactedText);

            int count = 0;
            StringBuffer sb = new StringBuffer();

            while (m.find()) {
                // Match ko replace karo
                String replacement =
                        "[" + piiType + "_REDACTED]";
                m.appendReplacement(sb,
                        Matcher.quoteReplacement(replacement));
                count++;
            }
            m.appendTail(sb);

            if (count > 0) {
                redactedText = sb.toString();
                breakdown.put(piiType, count);
                totalCount += count;
            }
        }

        // Result return karo
        String message = totalCount > 0
                ? totalCount + " sensitive items redacted ✅"
                : "Koi sensitive data nahi mila";

        return new RedactionResult(
                redactedText,
                totalCount,
                breakdown,
                true,
                message
        );
    }

    // ========================================
    // PREVIEW METHOD — Pehle 5 matches dikhao
    // ========================================
    public List<String> previewPii(String text) {
        List<String> found = new ArrayList<>();

        for (Map.Entry<String, String> entry
                : PII_PATTERNS.entrySet()) {
            Pattern p = Pattern.compile(entry.getValue());
            Matcher m = p.matcher(text);

            while (m.find() && found.size() < 5) {
                found.add(entry.getKey()
                        + ": " + m.group());
            }
        }
        return found;
    }
}
