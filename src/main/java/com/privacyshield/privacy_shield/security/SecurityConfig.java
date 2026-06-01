package com.privacyshield.privacy_shield.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.*;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication
        .UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // ✅ Ab JwtAuthFilter inject hoga
    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http) throws Exception {

        http
                // CSRF disable — REST API ke liye
                .csrf(csrf -> csrf.disable())

                // CORS enable — React frontend ke liye
                .cors(cors -> cors.configurationSource(
                        corsConfigurationSource()))

                // Session nahi chahiye — JWT use karenge
                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))

                // URL permissions
                .authorizeHttpRequests(auth -> auth
                        // Public URLs
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/payment/webhook",
                                "/health",
                                "/actuator/**"
                        ).permitAll()
                        // Baaki sab ke liye JWT chahiye
                        .anyRequest().authenticated()
                )

                // ✅ JWT filter add karo
                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    // ✅ CORS Config — React localhost:5173 allow
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(
                "http://localhost:5173",  // Vite dev server
                "http://localhost:3000",  // CRA dev server
                "https://privacyshield.in" // Production
        ));

        config.setAllowedMethods(
                List.of("GET","POST","PUT",
                        "DELETE","OPTIONS"));

        config.setAllowedHeaders(
                List.of("Authorization",
                        "Content-Type",
                        "X-Requested-With"));

        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
