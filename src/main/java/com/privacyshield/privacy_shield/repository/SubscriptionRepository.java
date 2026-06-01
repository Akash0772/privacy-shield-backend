package com.privacyshield.privacy_shield.repository;


import com.privacyshield.privacy_shield.entity.Subscription;
import com.privacyshield.privacy_shield.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SubscriptionRepository
        extends JpaRepository<Subscription, Long> {

    // User ki subscription dhundho
    Optional<Subscription> findByUser(User user);

    // Active subscription hai?
    boolean existsByUserAndIsActiveTrue(User user);
}
