package com.smms.backend.repository;

import com.smms.backend.model.AuditLog;
import com.smms.backend.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    List<AuditLog> findByUserOrderByTimestampDesc(User user, Pageable pageable);
    
    List<AuditLog> findByUserAndStatusOrderByTimestampDesc(User user, String status, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("""
            SELECT a FROM AuditLog a
            JOIN FETCH a.user
            WHERE a.user = :user
            ORDER BY a.timestamp DESC
            """)
    List<AuditLog> findRecentByUserWithUser(@org.springframework.data.repository.query.Param("user") User user, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("""
            SELECT a FROM AuditLog a
            JOIN FETCH a.user
            WHERE a.user = :user AND a.status = :status
            ORDER BY a.timestamp DESC
            """)
    List<AuditLog> findRecentByUserAndStatusWithUser(
            @org.springframework.data.repository.query.Param("user") User user,
            @org.springframework.data.repository.query.Param("status") String status,
            Pageable pageable
    );
    
    List<AuditLog> findTop50ByUserOrderByTimestampDesc(User user);
}
