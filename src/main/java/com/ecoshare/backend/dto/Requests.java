package com.ecoshare.backend.dto;

public class Requests {

    public static class LoginRequest {
        private String email;
        private String password;
        private String type;
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }

    public static class RegisterRequest {
        private String name;
        private String email;
        private String password;
        private String type;
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }

    public static class PasswordUpdateRequest {
        private String password;
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class ProfileUpdateRequest {
        private String name;
        private String phone;
        private String address;
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
    }

    public static class StatusUpdateRequest {
        private String status;
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class ListingRequest {
        private String title;
        private String expiry;
        private String distance;
        private String quantity;
        private String time;
        private String location;
        private String contact;
        private Boolean isUrgent;
        private String status; 
        private Boolean pickedUp;
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getExpiry() { return expiry; }
        public void setExpiry(String expiry) { this.expiry = expiry; }
        public String getDistance() { return distance; }
        public void setDistance(String distance) { this.distance = distance; }
        public String getQuantity() { return quantity; }
        public void setQuantity(String quantity) { this.quantity = quantity; }
        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public String getContact() { return contact; }
        public void setContact(String contact) { this.contact = contact; }
        public Boolean getIsUrgent() { return isUrgent; }
        public void setIsUrgent(Boolean urgent) { isUrgent = urgent; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Boolean getPickedUp() { return pickedUp; }
        public void setPickedUp(Boolean pickedUp) { this.pickedUp = pickedUp; }
    }

    public static class ReportRequest {
        private Long listing_id;
        private String reason;
        
        public Long getListing_id() { return listing_id; }
        public void setListing_id(Long listing_id) { this.listing_id = listing_id; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}
