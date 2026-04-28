package com.ecoshare.backend.repository;

import com.ecoshare.backend.entity.Listing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ListingRepository extends JpaRepository<Listing, Long> {
    List<Listing> findAllByOrderByCreatedAtDesc();
    
    @Query("SELECT COUNT(r) FROM Report r WHERE r.listing.id = :listingId AND r.status = 'Pending'")
    long countPendingReports(Long listingId);
}
