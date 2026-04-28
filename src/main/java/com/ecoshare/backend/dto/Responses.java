package com.ecoshare.backend.dto;

import com.ecoshare.backend.entity.Organization;

public class Responses {

    public static class JwtResponse {
        private String token;
        private Organization user;
        
        public JwtResponse(String token, Organization user) {
            this.token = token;
            this.user = user;
        }
        
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public Organization getUser() { return user; }
        public void setUser(Organization user) { this.user = user; }
    }

    public static class MessageResponse {
        private String message;
        
        public MessageResponse(String message) { this.message = message; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class SuccessResponse {
        private boolean success;
        
        public SuccessResponse(boolean success) { this.success = success; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
    }
    
    public static class SuccessIdResponse {
        private Long id;
        private boolean success;
        
        public SuccessIdResponse(Long id, boolean success) {
            this.id = id;
            this.success = success;
        }
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
    }
}
