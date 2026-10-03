package com.smms.backend.security;

import com.smms.backend.model.User;
import com.smms.backend.repository.UserRepository;
import com.smms.backend.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

/**
 * JWT Authentication Filter that validates JWT tokens and sets the security context.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        // Always start with a clean thread-local context to prevent stale user leakage
        // across requests handled by reused container threads.
        AuthContext.clear();

        final String authHeader = request.getHeader("Authorization");
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                final String jwt = authHeader.substring(7);

                try {
                    if (jwtUtil.validateToken(jwt) && SecurityContextHolder.getContext().getAuthentication() == null) {
                        String email = jwtUtil.extractEmail(jwt);
                        String role = jwtUtil.extractRole(jwt);

                        Optional<User> userOpt = userRepository.findByEmail(email);

                        if (userOpt.isPresent()) {
                            User user = userOpt.get();

                            // Set auth context for service-layer user scoping
                            AuthContext.setCurrentUser(user);

                            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                    user,
                                    null,
                                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                            );

                            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authToken);
                        }
                    }
                } catch (Exception e) {
                    // Token validation failed, continue without authentication
                    logger.debug("JWT validation failed: " + e.getMessage());
                }
            }

            filterChain.doFilter(request, response);
        } finally {
            // Clear AuthContext after request completes
            AuthContext.clear();
        }
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.equals("/api/login") || 
               path.equals("/api/register") ||
               path.equals("/api/forgot-password") ||
               path.equals("/api/verify-otp") ||
               path.equals("/api/reset-password") ||
               path.equals("/api/landing") ||
               path.startsWith("/api/landing/") ||
               path.startsWith("/api/public/") ||
               path.startsWith("/uploads/") ||
               path.startsWith("/h2-console");
    }
}
