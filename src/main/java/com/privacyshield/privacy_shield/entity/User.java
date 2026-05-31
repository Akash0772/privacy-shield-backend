package com.privacyshield.privacy_shield.entity;


import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data                    // Lombok — getter/setter auto banata hai
@Entity                  // Yeh ek database table hai
@Table(name = "users")   // Table ka naam
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;          // Encrypted store hoga

    @Column(name = "ca_firm_name")
    private String caFirmName;

    @Column(name = "plan")
    private String plan = "FREE";     // Default FREE plan

    @Column(name = "documents_used")
    private Integer documentsUsed = 0; // Is month kitne documents

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
