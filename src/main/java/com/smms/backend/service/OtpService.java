package com.smms.backend.service;

import com.smms.backend.model.PasswordResetOtp;
import com.smms.backend.model.User;
import com.smms.backend.repository.PasswordResetOtpRepository;
import com.smms.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.MessagingException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service for OTP generation, validation, and password reset.
 */
@Service
public class OtpService {

    @Autowired
    private PasswordResetOtpRepository otpRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Value("${otp.expiration.minutes:5}")
    private int otpExpirationMinutes;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generate and send OTP to user's email.
     */
    @Transactional
    public String sendOtp(String email) throws MessagingException {
        // Check if user exists
        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("No account found with this email address"));

        // Invalidate any previous OTPs
        otpRepository.invalidateAllOtpsForEmail(email.toLowerCase());

        // Generate new 6-digit OTP
        String otp = generateOtp();

        // Save OTP to database
        PasswordResetOtp otpEntity = new PasswordResetOtp(email.toLowerCase(), otp, otpExpirationMinutes);
        otpRepository.save(otpEntity);

        // Send email
        emailService.sendOtpEmail(email, otp, user.getFullName());
        return otp;
    }

    /**
     * Verify OTP and return true if valid.
     * Note: Does NOT mark OTP as used - that happens in resetPassword.
     */
    @Transactional(readOnly = true)
    public boolean verifyOtp(String email, String otp) {
        Optional<PasswordResetOtp> otpOpt = otpRepository.findValidOtpByEmail(
                email.toLowerCase(), 
                LocalDateTime.now()
        );

        if (otpOpt.isEmpty()) {
            return false;
        }

        PasswordResetOtp otpEntity = otpOpt.get();
        
        // Just verify, don't mark as used yet - that happens when password is reset
        return otpEntity.getOtp().equals(otp);
    }

    /**
     * Reset password after OTP verification.
     */
    @Transactional
    public void resetPassword(String email, String otp, String newPassword) {
        // Verify OTP first
        Optional<PasswordResetOtp> otpOpt = otpRepository.findValidOtpByEmail(
                email.toLowerCase(), 
                LocalDateTime.now()
        );

        if (otpOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid or expired OTP. Please request a new one.");
        }

        PasswordResetOtp otpEntity = otpOpt.get();

        if (!otpEntity.getOtp().equals(otp)) {
            throw new IllegalArgumentException("Invalid OTP. Please try again.");
        }

        // Get user and update password
        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Hash password (using same method as registration)
        user.setPasswordHash(hashPassword(newPassword));
        userRepository.save(user);

        // Mark OTP as used
        otpEntity.setUsed(true);
        otpRepository.save(otpEntity);
    }

    /**
     * Generate 6-digit OTP.
     */
    private String generateOtp() {
        int otp = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(otp);
    }

    /**
     * Simple SHA-256 hash (same as AuthService).
     */
    private String hashPassword(String password) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * Cleanup expired OTPs (can be called by a scheduled job).
     */
    @Transactional
    public void cleanupExpiredOtps() {
        otpRepository.deleteExpiredOtps(LocalDateTime.now());
    }
}
