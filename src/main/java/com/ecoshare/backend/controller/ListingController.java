package com.ecoshare.backend.controller;

import com.ecoshare.backend.dto.Requests.ListingRequest;
import com.ecoshare.backend.dto.Responses.SuccessIdResponse;
import com.ecoshare.backend.dto.Responses.SuccessResponse;
import com.ecoshare.backend.entity.Listing;
import com.ecoshare.backend.entity.Organization;
import com.ecoshare.backend.repository.ListingRepository;
import com.ecoshare.backend.repository.OrganizationRepository;
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
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/listings")
public class ListingController {

    @Autowired
    ListingRepository listingRepository;

    @Autowired
    OrganizationRepository orgRepository;

    @GetMapping
    public ResponseEntity<?> getListings(@RequestParam(defaultValue = "1000") int limit, @RequestParam(defaultValue = "0") int offset) {
        List<Listing> allListings = listingRepository.findAllByOrderByCreatedAtDesc();

        // Simulate limit and offset
        int start = Math.min(offset, allListings.size());
        int end = Math.min(start + limit, allListings.size());
        List<Listing> pagedListings = allListings.subList(start, end);

        List<Map<String, Object>> response = new ArrayList<>();

        for (Listing l : pagedListings) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", l.getId());
            map.put("title", l.getTitle());
            map.put("expiry", l.getExpiry());
            map.put("status", l.getStatus());
            map.put("distance", l.getDistance());
            map.put("quantity", l.getQuantity());
            map.put("time", l.getTime());
            map.put("location", l.getLocation());
            map.put("donor_id", l.getDonor().getId());
            map.put("donorName", l.getDonor().getName());
            map.put("contact", l.getContact());
            map.put("isUrgent", l.getIsUrgent());
            map.put("claimed", l.getClaimedBy() != null);
            map.put("claimedBy", l.getClaimedBy() != null ? l.getClaimedBy().getName() : null);
            map.put("claimed_by_id", l.getClaimedBy() != null ? l.getClaimedBy().getId() : null);
            map.put("pickedUp", l.getPickedUp());
            map.put("created_at", l.getCreatedAt());
            map.put("reports_count", listingRepository.countPendingReports(l.getId()));
            response.add(map);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> createListing(@RequestBody ListingRequest request, Authentication authentication) {
        if (request.getTitle() == null || request.getQuantity() == null || request.getLocation() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Title, quantity, and location are required."));
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Organization donor = orgRepository.findById(userDetails.getId()).orElse(null);

        Listing listing = new Listing();
        listing.setTitle(request.getTitle().replaceAll("[<>]", ""));
        listing.setExpiry(request.getExpiry());
        listing.setDistance(request.getDistance());
        listing.setQuantity(request.getQuantity());
        listing.setTime(request.getTime());
        listing.setLocation(request.getLocation().replaceAll("[<>]", ""));
        listing.setContact(request.getContact() != null ? request.getContact().replaceAll("[<>]", "") : null);
        listing.setIsUrgent(request.getIsUrgent() != null ? request.getIsUrgent() : false);
        listing.setDonor(donor);

        Listing saved = listingRepository.save(listing);
        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessIdResponse(saved.getId(), true));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateListing(@PathVariable Long id, @RequestBody ListingRequest request, Authentication authentication) {
        Optional<Listing> listingData = listingRepository.findById(id);

        if (listingData.isPresent()) {
            Listing listing = listingData.get();

            if (request.getTitle() != null) listing.setTitle(request.getTitle().replaceAll("[<>]", ""));
            if (request.getQuantity() != null) listing.setQuantity(request.getQuantity());
            if (request.getTime() != null) listing.setTime(request.getTime().replaceAll("[<>]", ""));
            if (request.getLocation() != null) listing.setLocation(request.getLocation().replaceAll("[<>]", ""));
            if (request.getStatus() != null) listing.setStatus(request.getStatus());
            if (request.getIsUrgent() != null) listing.setIsUrgent(request.getIsUrgent());
            if (request.getPickedUp() != null) listing.setPickedUp(request.getPickedUp());

            listingRepository.save(listing);
            return ResponseEntity.ok(new SuccessResponse(true));
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Listing not found"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteListing(@PathVariable Long id, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Optional<Listing> listingData = listingRepository.findById(id);

        if (listingData.isPresent()) {
            Listing listing = listingData.get();
            if (!"Admin".equals(userDetails.getType()) && !listing.getDonor().getId().equals(userDetails.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Unauthorized to delete this listing"));
            }

            listingRepository.deleteById(id);
            return ResponseEntity.ok(new SuccessResponse(true));
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Listing not found"));
    }

    @PatchMapping("/{id}/claim")
    public ResponseEntity<?> claimListing(@PathVariable Long id, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        if (!"Recipient".equals(userDetails.getType())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Only recipients can claim"));
        }

        Optional<Listing> listingData = listingRepository.findById(id);
        if (listingData.isPresent()) {
            Listing listing = listingData.get();

            if (!"Available".equals(listing.getStatus()) || listing.getClaimedBy() != null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Listing not available or already claimed"));
            }

            Organization recipient = orgRepository.findById(userDetails.getId()).orElse(null);
            listing.setStatus("Claimed");
            listing.setClaimedBy(recipient);
            listingRepository.save(listing);

            return ResponseEntity.ok(new SuccessResponse(true));
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Listing not found"));
    }

    @PatchMapping("/{id}/unclaim")
    public ResponseEntity<?> unclaimListing(@PathVariable Long id, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        if (!"Recipient".equals(userDetails.getType())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Only recipients can unclaim"));
        }

        Optional<Listing> listingData = listingRepository.findById(id);
        if (listingData.isPresent()) {
            Listing listing = listingData.get();

            if (listing.getClaimedBy() == null || !listing.getClaimedBy().getId().equals(userDetails.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You did not claim this listing"));
            }

            if ("PickedUp".equals(listing.getStatus()) || "Rescued".equals(listing.getStatus())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Cannot unclaim picked up item"));
            }

            listing.setStatus("Available");
            listing.setClaimedBy(null);
            listingRepository.save(listing);

            return ResponseEntity.ok(new SuccessResponse(true));
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Listing not found"));
    }

    @PatchMapping("/{id}/pickup")
    public ResponseEntity<?> pickupListing(@PathVariable Long id, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Optional<Listing> listingData = listingRepository.findById(id);

        if (listingData.isPresent()) {
            Listing listing = listingData.get();

            if (!"Claimed".equals(listing.getStatus()) || listing.getClaimedBy() == null || !listing.getClaimedBy().getId().equals(userDetails.getId())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Must be actively claimed by you"));
            }

            listing.setStatus("PickedUp");
            listing.setPickedUp(true);
            listingRepository.save(listing);

            return ResponseEntity.ok(new SuccessResponse(true));
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Listing not found"));
    }
}
