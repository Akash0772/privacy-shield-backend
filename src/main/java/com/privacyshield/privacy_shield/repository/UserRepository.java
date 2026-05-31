package com.privacyshield.privacy_shield.repository;


import com.privacyshield.privacy_shield.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository
        extends JpaRepository<User, Long> {

    // Email se user dhundho
    Optional<User> findByEmail(String email);

    // Email already exist karta hai?
    boolean existsByEmail(String email);
}
