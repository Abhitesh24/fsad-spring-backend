package com.ecoshare.backend.controller;

import com.ecoshare.backend.dto.Requests.ReportRequest;
import com.ecoshare.backend.dto.Responses.SuccessResponse;
import com.ecoshare.backend.entity.Listing;
import com.ecoshare.backend.entity.Organization;
import com.ecoshare.backend.entity.Report;
import com.ecoshare.backend.repository.ListingRepository;
import com.ecoshare.backend.repository.OrganizationRepository;
import com.ecoshare.backend.repository.ReportRepository;
import com.ecoshare.backend.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    ReportRepository reportRepository;

    @Autowired
    ListingRepository listingRepository;

    @Autowired
    OrganizationRepository orgRepository;

    @PostMapping
    public ResponseEntity<?> submitReport(@RequestBody ReportRequest request, Authentication authentication) {
        if (request.getListing_id() == null || request.getReason() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing listing ID or reason"));
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Organization reporter = orgRepository.findById(userDetails.getId()).orElse(null);
        Listing listing = listingRepository.findById(request.getListing_id()).orElse(null);

        if (listing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Listing not found"));
        }

        Report report = new Report();
        report.setListing(listing);
        report.setReportedBy(reporter);
        report.setReason(request.getReason());
        report.setStatus("Pending");

        reportRepository.save(report);

        return ResponseEntity.ok(new SuccessResponse(true));
    }

    @GetMapping
    public ResponseEntity<?> getReports(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        if (!"Admin".equals(userDetails.getType())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Admin access required."));
        }

        List<Report> reports = reportRepository.findAllByOrderByCreatedAtDesc();
        List<Map<String, Object>> response = new ArrayList<>();

        for (Report r : reports) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("reason", r.getReason());
            map.put("status", r.getStatus());
            map.put("created_at", r.getCreatedAt());
            map.put("listing_title", r.getListing().getTitle());
            map.put("listing_id", r.getListing().getId());
            map.put("reporter_name", r.getReportedBy().getName());
            map.put("reporter_id", r.getReportedBy().getId());
            response.add(map);
        }

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<?> resolveReport(@PathVariable Long id, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        if (!"Admin".equals(userDetails.getType())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Admin access required."));
        }

        Report report = reportRepository.findById(id).orElse(null);
        if (report != null) {
            report.setStatus("Resolved");
            reportRepository.save(report);
            return ResponseEntity.ok(new SuccessResponse(true));
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Report not found"));
    }
}
