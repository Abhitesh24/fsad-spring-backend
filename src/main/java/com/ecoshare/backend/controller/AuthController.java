package com.ecoshare.backend.controller;

import com.ecoshare.backend.dto.Requests.LoginRequest;
import com.ecoshare.backend.dto.Requests.PasswordUpdateRequest;
import com.ecoshare.backend.dto.Requests.RegisterRequest;
import com.ecoshare.backend.dto.Responses.JwtResponse;
import com.ecoshare.backend.dto.Responses.SuccessResponse;
import com.ecoshare.backend.entity.Organization;
import com.ecoshare.backend.repository.OrganizationRepository;
import com.ecoshare.backend.security.JwtUtils;
import com.ecoshare.backend.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    OrganizationRepository orgRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    private String normalizeType(String str) {
        if (str == null) return null;
        String lower = str.toLowerCase();
        return lower.substring(0, 1).toUpperCase() + lower.substring(1);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest signUpRequest) {
        if (signUpRequest.getEmail() == null || signUpRequest.getPassword() == null || signUpRequest.getType() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields"));
        }

        String type = normalizeType(signUpRequest.getType());

        if ("Admin".equals(type)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Cannot register as Admin via this endpoint"));
        }

        if (orgRepository.existsByEmail(signUpRequest.getEmail().toLowerCase())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already exists"));
        }

        String orgName = signUpRequest.getName() != null ? signUpRequest.getName().replaceAll("[<>]", "") : signUpRequest.getEmail().split("@")[0];

        Organization org = new Organization();
        org.setName(orgName);
        org.setEmail(signUpRequest.getEmail().toLowerCase());
        org.setPassword(encoder.encode(signUpRequest.getPassword()));
        org.setType(type);
        org.setStatus("Approved");

        orgRepository.save(org);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(org.getEmail(), signUpRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        org.setPassword(null); // Do not return password
        return ResponseEntity.status(HttpStatus.CREATED).body(new JwtResponse(jwt, org));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");
        String type = request.get("type");

        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty() || type == null || type.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing credentials"));
        }

        Organization org = orgRepository.findByEmail(email.toLowerCase()).orElse(null);
        
        if (org == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid Email or Password"));
        }

        if (!encoder.matches(password, org.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid Email or Password"));
        }

        String normalizedType = normalizeType(type);
        if (!org.getType().equals(normalizedType)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Invalid Portal. This account is registered as a different role."));
        }

        if ("Rejected".equals(org.getStatus())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Account rejected by Admin."));
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email.toLowerCase(), password));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        org.setPassword(null);
        return ResponseEntity.ok(new JwtResponse(jwt, org));
    }

    @PutMapping("/password")
    public ResponseEntity<?> updatePassword(@RequestBody PasswordUpdateRequest request, Authentication authentication) {
        if (request.getPassword() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password cannot be empty"));
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Organization org = orgRepository.findById(userDetails.getId()).orElse(null);

        if (org != null) {
            org.setPassword(encoder.encode(request.getPassword()));
            orgRepository.save(org);
            return ResponseEntity.ok(new SuccessResponse(true));
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
    }
}
