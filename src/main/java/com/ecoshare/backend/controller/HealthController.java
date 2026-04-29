package com.ecoshare.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        
        try {
            jdbcTemplate.execute("SELECT 1");
            status.put("database", "CONNECTED");
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            status.put("database", "DISCONNECTED");
            status.put("error", e.getMessage());
            return ResponseEntity.status(503).body(status);
        }
    }
}
