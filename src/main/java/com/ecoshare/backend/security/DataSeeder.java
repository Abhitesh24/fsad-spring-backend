package com.ecoshare.backend.security;

import com.ecoshare.backend.entity.Organization;
import com.ecoshare.backend.repository.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        String adminEmail = "admin@gmail.com";
        if (!organizationRepository.existsByEmail(adminEmail)) {
            Organization admin = new Organization();
            admin.setName("Super Admin");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setType("Admin");
            admin.setStatus("Approved");
            organizationRepository.save(admin);
            System.out.println("Admin user seeded successfully!");
        }
    }
}
