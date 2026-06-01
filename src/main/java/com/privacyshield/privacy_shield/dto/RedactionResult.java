package com.privacyshield.privacy_shield.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.Map;

@Data
@AllArgsConstructor
public class RedactionResult {

    // Redacted text — clean version
    private String redactedText;

    // Total kitne PII items mile
    private int totalPiiFound;

    // Breakdown — PAN kitne, Phone kitne etc
    private Map<String, Integer> piiBreakdown;

    // Processing successful raha?
    private boolean success;

    // Koi message
    private String message;
}
