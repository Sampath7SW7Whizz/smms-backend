package com.smms.backend.repository;

import com.smms.backend.model.PasswordResetOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, Long> {

    /**
     * Find the latest valid (not used, not expired) OTP for an email.
     */
    @Query("SELECT o FROM PasswordResetOtp o WHERE o.email = :email AND o.used = false AND o.expiresAt > :now ORDER BY o.createdAt DESC LIMIT 1")
    Optional<PasswordResetOtp> findValidOtpByEmail(String email, LocalDateTime now);

    /**
     * Invalidate all previous OTPs for an email.
     */
    @Modifying
    @Transactional
    @Query("UPDATE PasswordResetOtp o SET o.used = true WHERE o.email = :email AND o.used = false")
    void invalidateAllOtpsForEmail(String email);

    /**
     * Delete expired OTPs (cleanup job).
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetOtp o WHERE o.expiresAt < :now")
    void deleteExpiredOtps(LocalDateTime now);
}
