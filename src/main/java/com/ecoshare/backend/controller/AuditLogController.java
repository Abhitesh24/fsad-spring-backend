package com.ecoshare.backend.controller;

import com.ecoshare.backend.dto.Requests.AuditLogRequest;
import com.ecoshare.backend.dto.Responses.SuccessResponse;
import com.ecoshare.backend.entity.AuditLog;
import com.ecoshare.backend.entity.Organization;
import com.ecoshare.backend.repository.AuditLogRepository;
import com.ecoshare.backend.repository.OrganizationRepository;
import com.ecoshare.backend.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    @Autowired
    AuditLogRepository auditLogRepository;

    @Autowired
    OrganizationRepository orgRepository;

    @GetMapping
    public ResponseEntity<?> getAuditLogs(Authentication authentication) {
        List<AuditLog> logs = auditLogRepository.findAllByOrderByTimestampDesc();
        
        List<Map<String, Object>> response = new ArrayList<>();
        for (AuditLog log : logs) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", log.getId());
            map.put("action", log.getAction());
            map.put("details", log.getDetails());
            map.put("timestamp", log.getTimestamp() != null ? log.getTimestamp().toString() : LocalDateTime.now().toString());
            response.add(map);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> createAuditLog(@RequestBody AuditLogRequest request, Authentication authentication) {
        AuditLog log = new AuditLog();
        log.setAction(request.getAction());
        log.setDetails(request.getDetails());
        
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Organization user = orgRepository.findById(userDetails.getId()).orElse(null);
            log.setUser(user);
        }

        auditLogRepository.save(log);
        return ResponseEntity.ok(new SuccessResponse(true));
    }
}
