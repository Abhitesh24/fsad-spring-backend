package com.ecoshare.backend.controller;

import com.ecoshare.backend.dto.Requests.ProfileUpdateRequest;
import com.ecoshare.backend.dto.Requests.StatusUpdateRequest;
import com.ecoshare.backend.dto.Responses.SuccessResponse;
import com.ecoshare.backend.entity.Listing;
import com.ecoshare.backend.entity.Organization;
import com.ecoshare.backend.repository.ListingRepository;
import com.ecoshare.backend.repository.OrganizationRepository;
import com.ecoshare.backend.repository.ReportRepository;
import com.ecoshare.backend.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api")
public class OrganizationController {

    @Autowired
    OrganizationRepository orgRepository;

    @Autowired
    ListingRepository listingRepository;

    @Autowired
    ReportRepository reportRepository;

    @GetMapping({"/orgs", "/organizations"})
    public ResponseEntity<?> getAllOrgs() {
        List<Organization> orgs = orgRepository.findAll();
        orgs.forEach(org -> org.setPassword(null));
        return ResponseEntity.ok(orgs);
    }

    @PutMapping("/orgs/{id}/status")
    public ResponseEntity<?> updateOrgStatus(@PathVariable Long id, @RequestBody StatusUpdateRequest request, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        if (!"Admin".equals(userDetails.getType())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Admin access required."));
        }

        Optional<Organization> orgData = orgRepository.findById(id);
        if (orgData.isPresent()) {
            Organization org = orgData.get();
            org.setStatus(request.getStatus());
            orgRepository.save(org);
            return ResponseEntity.ok(new SuccessResponse(true));
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Organization not found"));
    }

    @DeleteMapping("/orgs/{id}")
    public ResponseEntity<?> deleteOrg(@PathVariable Long id, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        if (!"Admin".equals(userDetails.getType())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Admin access required."));
        }

        Optional<Organization> orgData = orgRepository.findById(id);
        if (orgData.isPresent()) {
            Organization org = orgData.get();

            // Handle cascades similarly to Node.js backend
            // In a real app we'd let JPA cascade or handle carefully.
            // For this port, we will delete org and let JPA handle constraints or manually clean up if needed.
            // To be perfectly safe, we delete associated records manually just like the node app did.

            reportRepository.findAll().forEach(r -> {
                if (r.getReportedBy().getId().equals(id)) {
                    reportRepository.delete(r);
                }
            });

            listingRepository.findAll().forEach(l -> {
                if (l.getClaimedBy() != null && l.getClaimedBy().getId().equals(id)) {
                    l.setStatus("Available");
                    l.setClaimedBy(null);
                    l.setPickedUp(false);
                    listingRepository.save(l);
                }
                if (l.getDonor().getId().equals(id)) {
                    reportRepository.findAll().forEach(r -> {
                        if (r.getListing().getId().equals(l.getId())) {
                            reportRepository.delete(r);
                        }
                    });
                    listingRepository.delete(l);
                }
            });

            orgRepository.deleteById(id);
            return ResponseEntity.ok(new SuccessResponse(true));
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Organization not found"));
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody ProfileUpdateRequest request, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Optional<Organization> orgData = orgRepository.findById(userDetails.getId());

        if (orgData.isPresent()) {
            Organization org = orgData.get();
            if (request.getName() != null) org.setName(request.getName().replaceAll("[<>]", ""));
            if (request.getPhone() != null) org.setPhone(request.getPhone().replaceAll("[<>]", ""));
            if (request.getAddress() != null) org.setAddress(request.getAddress().replaceAll("[<>]", ""));
            orgRepository.save(org);
            return ResponseEntity.ok(new SuccessResponse(true));
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Organization not found"));
    }
}
