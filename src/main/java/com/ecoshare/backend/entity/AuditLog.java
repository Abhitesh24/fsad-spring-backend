package com.ecoshare.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Organization user;

    @Column(nullable = false)
    private String action;

    @Column(name = "target_id")
    private Long targetId;

    @Column(length = 1000)
    private String details;

    @Column(updatable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    public AuditLog() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Organization getUser() { return user; }
    public void setUser(Organization user) { this.user = user; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
