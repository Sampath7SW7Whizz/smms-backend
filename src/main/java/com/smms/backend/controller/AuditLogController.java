package com.smms.backend.controller;

import com.smms.backend.model.AuditLog;
import com.smms.backend.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/audit")
@CrossOrigin(origins = "http://localhost:5173")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping("/recent")
    public ResponseEntity<Map<String, Object>> getRecentLogs(
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "ALL") String filter) {
        
        List<AuditLog> logs = auditLogService.getRecentLogs(limit, filter);
        
        // Convert to DTO format for frontend
        List<Map<String, Object>> logDtos = logs.stream().map(log -> {
            Map<String, Object> dto = new HashMap<>();
            dto.put("id", log.getId());
            dto.put("actionType", log.getAction());
            dto.put("entityType", log.getEntityType());
            dto.put("entityId", log.getEntityId());
            dto.put("actionDescription", log.getDescription());
            dto.put("timestamp", log.getTimestamp());
            dto.put("status", log.getStatus());
            dto.put("userName", log.getUser() != null ? log.getUser().getFullName() : "Unknown");
            return dto;
        }).collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("logs", logDtos);
        response.put("total", logDtos.size());
        
        return ResponseEntity.ok(response);
    }
}
