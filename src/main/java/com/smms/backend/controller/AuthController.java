package com.smms.backend.controller;

import com.smms.backend.dto.ForgotPasswordRequest;
import com.smms.backend.dto.LoginRequest;
import com.smms.backend.dto.RegisterRequest;
import com.smms.backend.dto.ResetPasswordRequest;
import com.smms.backend.dto.VerifyOtpRequest;
import com.smms.backend.service.AuthService;
import com.smms.backend.service.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private OtpService otpService;

    @Value("${smms.mail.offline-mode:true}")
    private boolean offlineMailMode;

    /**
     * POST /api/register
     * Body: { fullName, email, phone, password, shopName, shopAddress, referredBy?
     * }
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        try {
            Map<String, Object> result = authService.register(req);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Registration failed: " + ex.getMessage()));
        }
    }

    /**
     * POST /api/login
     * Body: { email, password }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            Map<String, Object> result = authService.login(req);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Login failed: " + ex.getMessage()));
        }
    }

    /**
     * POST /api/forgot-password
     * Body: { email }
     * Sends OTP to the registered email.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest req) {
        try {
            if (req.getEmail() == null || req.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Email is required"));
            }

            String otp = otpService.sendOtp(req.getEmail().trim());

            Map<String, Object> response = new java.util.LinkedHashMap<>();
            response.put("message", offlineMailMode
                    ? "OTP generated locally. Check the response or backend logs."
                    : "OTP sent successfully to your email");
            response.put("email", maskEmail(req.getEmail().trim()));
            response.put("deliveryMode", offlineMailMode ? "offline-log" : "smtp");

            if (offlineMailMode) {
                response.put("debugOtp", otp);
            }

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to send OTP: " + ex.getMessage()));
        }
    }

    /**
     * POST /api/verify-otp
     * Body: { email, otp }
     * Verifies the OTP.
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest req) {
        try {
            if (req.getEmail() == null || req.getOtp() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Email and OTP are required"));
            }

            boolean isValid = otpService.verifyOtp(req.getEmail().trim(), req.getOtp().trim());
            
            if (isValid) {
                return ResponseEntity.ok(Map.of(
                        "message", "OTP verified successfully",
                        "verified", true
                ));
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid or expired OTP", "verified", false));
            }
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "OTP verification failed: " + ex.getMessage()));
        }
    }

    /**
     * POST /api/reset-password
     * Body: { email, otp, newPassword }
     * Resets the password after OTP verification.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest req) {
        try {
            if (req.getEmail() == null || req.getOtp() == null || req.getNewPassword() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Email, OTP, and new password are required"));
            }

            if (req.getNewPassword().length() < 6) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Password must be at least 6 characters"));
            }

            otpService.resetPassword(req.getEmail().trim(), req.getOtp().trim(), req.getNewPassword());
            
            return ResponseEntity.ok(Map.of(
                    "message", "Password reset successfully. You can now login with your new password."
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Password reset failed: " + ex.getMessage()));
        }
    }

    /**
     * Mask email for privacy (e.g., j***@gmail.com)
     */
    private String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) return email;
        
        return email.charAt(0) + "***" + email.substring(atIndex);
    }
}
