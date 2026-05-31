package com.privacyshield.privacy_shield.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    // Secret key — production mein env variable mein rakhna
    private static final String SECRET =
            "privacyshield-secret-key-minimum-256-bits-long-string";

    private static final long EXPIRY =
            7 * 24 * 60 * 60 * 1000L; // 7 din

    private Key getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    // Token banao
    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRY))
                .signWith(getKey())
                .compact();
    }

    // Token se email nikalo
    public String getEmail(String token) {
        return Jwts.parser()
                .verifyWith((javax.crypto.SecretKey) getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // Token valid hai?
    public boolean isValid(String token) {
        try {
            getEmail(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
