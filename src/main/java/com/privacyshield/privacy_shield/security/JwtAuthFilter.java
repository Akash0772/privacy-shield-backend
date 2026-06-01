package com.privacyshield.privacy_shield.security;

import com.privacyshield.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Header se token nikalo
        String authHeader = request.getHeader("Authorization");

        // Token nahi hai — skip karo
        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. "Bearer " hata ke sirf token lo
        String token = authHeader.substring(7);

        try {
            // 3. Token valid hai?
            if (!jwtUtil.isValid(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            // 4. Token se email nikalo
            String email = jwtUtil.getEmail(token);

            // 5. Already authenticated hai?
            if (SecurityContextHolder.getContext()
                    .getAuthentication() != null) {
                filterChain.doFilter(request, response);
                return;
            }

            // 6. User database mein hai?
            var user = userRepository
                    .findByEmail(email)
                    .orElse(null);

            if (user == null) {
                filterChain.doFilter(request, response);
                return;
            }

            // 7. Spring Security ko batao —
            //    yeh user authenticated hai
            UserDetails userDetails =
                    org.springframework.security.core
                            .userdetails.User
                            .withUsername(email)
                            .password(user.getPassword())
                            .roles("USER")
                            .build();

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            userDetails, null,
                            userDetails.getAuthorities());

            authToken.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request));

            SecurityContextHolder.getContext()
                    .setAuthentication(authToken);

        } catch (Exception e) {
            // Invalid token — silently skip
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
