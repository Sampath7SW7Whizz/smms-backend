package com.smms.backend.service;

import com.smms.backend.model.AuditLog;
import com.smms.backend.model.User;
import com.smms.backend.repository.AuditLogRepository;
import com.smms.backend.security.AuthContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    /**
     * Log an action performed by the current user
     */
    public void log(String action, String entityType, Long entityId, String description) {
        try {
            User user = AuthContext.getCurrentUser();
            if (user != null) {
                AuditLog auditLog = new AuditLog(user, action, entityType, entityId, description);
                auditLogRepository.save(auditLog);
            }
        } catch (Exception e) {
            // Don't let audit logging failures break the main functionality
            System.err.println("Failed to log audit: " + e.getMessage());
        }
    }

    /**
     * Log an action with a specific status
     */
    public void log(String action, String entityType, Long entityId, String description, String status) {
        try {
            User user = AuthContext.getCurrentUser();
            if (user != null) {
                AuditLog auditLog = new AuditLog(user, action, entityType, entityId, description);
                auditLog.setStatus(status);
                auditLogRepository.save(auditLog);
            }
        } catch (Exception e) {
            System.err.println("Failed to log audit: " + e.getMessage());
        }
    }

    /**
     * Get recent audit logs for the current user
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getRecentLogs(int limit) {
        User user = AuthContext.getCurrentUser();
        if (user == null) {
            return List.of();
        }
        return auditLogRepository.findRecentByUserWithUser(user, PageRequest.of(0, limit));
    }

    /**
     * Get recent audit logs filtered by status
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getRecentLogs(int limit, String status) {
        User user = AuthContext.getCurrentUser();
        if (user == null) {
            return List.of();
        }
        if ("ALL".equalsIgnoreCase(status)) {
            return auditLogRepository.findRecentByUserWithUser(user, PageRequest.of(0, limit));
        }
        return auditLogRepository.findRecentByUserAndStatusWithUser(user, status, PageRequest.of(0, limit));
    }
}
