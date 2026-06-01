package com.privacyshield.privacy_shield.repository;

import com.privacyshield.privacy_shield.entity.AuditLog;
import com.privacyshield.privacy_shield.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogRepository
        extends JpaRepository<AuditLog, Long> {

    // Ek user ke saare logs
    List<AuditLog> findByUserOrderByProcessedAtDesc(User user);

    // Is mahine kitne documents process hue
    long countByUser(User user);
}
