package com.privacyshield.privacy_shield.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Kisne process kiya
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Kaunsi file
    @Column(name = "file_name")
    private String fileName;

    // File type — PDF ya DOCX
    @Column(name = "file_type")
    private String fileType;

    // Kitne PII items mile
    @Column(name = "pii_count")
    private Integer piiCount;

    // Konse type ke PII mile — JSON format
    // Example: {"PAN":2, "PHONE":5, "EMAIL":1}
    @Column(name = "pii_breakdown", columnDefinition = "TEXT")
    private String piiBreakdown;

    // Kab process hua
    @Column(name = "processed_at")
    private LocalDateTime processedAt = LocalDateTime.now();
}