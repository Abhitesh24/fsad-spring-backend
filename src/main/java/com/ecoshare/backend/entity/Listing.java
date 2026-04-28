package com.ecoshare.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "listings")
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String expiry;

    private String status = "Available";

    private String distance;
    private String quantity;
    private String time;
    private String location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donor_id", nullable = false)
    private Organization donor;

    private String contact;

    @Column(name = "is_urgent")
    private Boolean isUrgent = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claimed_by_id")
    private Organization claimedBy;

    @Column(name = "picked_up")
    private Boolean pickedUp = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Listing() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getExpiry() { return expiry; }
    public void setExpiry(String expiry) { this.expiry = expiry; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDistance() { return distance; }
    public void setDistance(String distance) { this.distance = distance; }
    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Organization getDonor() { return donor; }
    public void setDonor(Organization donor) { this.donor = donor; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    public Boolean getIsUrgent() { return isUrgent; }
    public void setIsUrgent(Boolean urgent) { isUrgent = urgent; }
    public Organization getClaimedBy() { return claimedBy; }
    public void setClaimedBy(Organization claimedBy) { this.claimedBy = claimedBy; }
    public Boolean getPickedUp() { return pickedUp; }
    public void setPickedUp(Boolean pickedUp) { this.pickedUp = pickedUp; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
