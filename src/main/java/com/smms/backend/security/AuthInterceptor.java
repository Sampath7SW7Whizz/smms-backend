package com.smms.backend.security;

import com.smms.backend.model.User;
import com.smms.backend.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

/**
 * Interceptor that validates the Bearer token in the Authorization header.
 * If valid, it populates the AuthContext with the corresponding User.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            Optional<User> userOpt = userRepository.findByToken(token);

            if (userOpt.isPresent()) {
                AuthContext.setCurrentUser(userOpt.get());
                return true;
            }
        }

        // For simplicity, we allow the request to proceed but AuthContext will be
        // empty.
        // The specific controller or service can then decide if they require a user.
        // However, it's safer to return true here and let the controllers handle 401 if
        // needed,
        // OR we can block it here for specific paths.

        // For now, let's allow it to proceed. We will filter data based on AuthContext
        // presence.
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        AuthContext.clear();
    }
}
