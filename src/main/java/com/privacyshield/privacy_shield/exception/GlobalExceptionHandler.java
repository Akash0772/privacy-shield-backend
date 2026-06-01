package com.privacyshield.privacy_shield.exception;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Runtime errors — jaise "User nahi mila"
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>>
    handleRuntime(RuntimeException ex) {

        Map<String, String> error = new HashMap<>();
        error.put("status", "ERROR");
        error.put("message", ex.getMessage());

        return ResponseEntity
                .badRequest()
                .body(error);
    }

    // File too large
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>>
    handleFileTooLarge(
            MaxUploadSizeExceededException ex) {

        Map<String, String> error = new HashMap<>();
        error.put("status", "ERROR");
        error.put("message",
                "File too large! Max 10MB allowed.");

        return ResponseEntity
                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(error);
    }

    // Sab baaki errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>>
    handleGeneral(Exception ex) {

        Map<String, String> error = new HashMap<>();
        error.put("status", "ERROR");
        error.put("message",
                "Kuch galat hua: " + ex.getMessage());

        return ResponseEntity
                .internalServerError()
                .body(error);
    }
}
