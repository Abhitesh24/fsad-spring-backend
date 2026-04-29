package com.ecoshare.backend.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;

@Component("configValidator")
public class ConfigValidator implements BeanFactoryPostProcessor, EnvironmentAware {

    private static final Logger logger = LoggerFactory.getLogger(ConfigValidator.class);
    private Environment env;

    @Override
    public void setEnvironment(Environment environment) {
        this.env = environment;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        logger.info("Initializing Render deployment validations early in startup...");

        String port = System.getenv("PORT");
        String dbUrl = System.getenv("DB_URL");
        String dbUsername = System.getenv("DB_USERNAME");
        String dbPassword = System.getenv("DB_PASSWORD");
        String frontendUrl = System.getenv("FRONTEND_URL");
        String jwtSecret = System.getenv("JWT_SECRET");

        // 1. Port Validation
        if (port == null || port.trim().isEmpty()) {
            logger.warn("ENVIRONMENT: PORT environment variable is missing. Falling back to 8080.");
            port = "8080";
        }
        try {
            Integer.parseInt(port);
            logger.info("ENVIRONMENT: Binding to PORT: {}", port);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("FATAL: PORT environment variable must be numeric.");
        }

        // 2. Database URL Validation
        if (dbUrl == null || dbUrl.trim().isEmpty()) {
            throw new IllegalStateException("FATAL: DB_URL environment variable is missing.");
        }
        if (dbUrl.contains("localhost") || dbUrl.contains("127.0.0.1")) {
            throw new IllegalStateException("FATAL: DB_URL contains localhost. A production database must be used.");
        }
        if (!dbUrl.startsWith("jdbc:mysql://") || !dbUrl.contains("?useSSL=false") || !dbUrl.contains("allowPublicKeyRetrieval=true")) {
            throw new IllegalStateException("FATAL: DB_URL must follow the complete MySQL format required.");
        }
        
        String dbHost = "Unknown";
        try {
            if (dbUrl.contains("jdbc:mysql://")) {
                dbHost = dbUrl.substring(13, dbUrl.indexOf(":", 13) > 0 ? dbUrl.indexOf(":", 13) : dbUrl.indexOf("/", 13));
            }
        } catch (Exception ignored) {}
        logger.info("ENVIRONMENT: Database configured with HOST: {}", dbHost);

        // 3. Database Credentials Validation
        if (dbUsername == null || dbUsername.trim().isEmpty()) {
            throw new IllegalStateException("FATAL: DB_USERNAME is missing or empty.");
        }
        if (dbPassword == null || dbPassword.trim().isEmpty()) {
            throw new IllegalStateException("FATAL: DB_PASSWORD is missing or empty.");
        }

        // 4. Frontend URL Validation
        if (frontendUrl == null || frontendUrl.trim().isEmpty()) {
            throw new IllegalStateException("FATAL: FRONTEND_URL environment variable is missing.");
        }
        if (frontendUrl.equals("*")) {
            throw new IllegalStateException("FATAL: FRONTEND_URL cannot be a wildcard (*). Specific production URL is required.");
        }
        logger.info("ENVIRONMENT: Allowed CORS Frontend: {}", frontendUrl);

        // 5. JWT Secret Validation
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            throw new IllegalStateException("FATAL: JWT_SECRET environment variable is missing.");
        }
        if (jwtSecret.length() < 32) {
            throw new IllegalStateException("FATAL: JWT_SECRET must be at least 32 characters long.");
        }
        String lowerSecret = jwtSecret.toLowerCase();
        if (lowerSecret.contains("123456") || lowerSecret.contains("password") || lowerSecret.contains("secret")) {
            throw new IllegalStateException("FATAL: JWT_SECRET is too weak. Do not use common dictionary words.");
        }

        // 6. Database Connection Test with Retry Mechanism
        boolean dbConnected = false;
        int maxRetries = 5;
        int attempt = 1;
        
        while (attempt <= maxRetries && !dbConnected) {
            try {
                logger.info("Attempting database connection... (Attempt {}/{})", attempt, maxRetries);
                // Use standard JDBC to test connection before Hikari/Hibernate starts
                try (Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
                    if (conn.isValid(5)) {
                        dbConnected = true;
                        logger.info("SUCCESS: Database connection verified and active.");
                    }
                }
            } catch (Exception e) {
                logger.warn("Database connection failed on attempt {}/{} due to: {}", attempt, maxRetries, e.getMessage());
                if (attempt == maxRetries) {
                    logger.error("FATAL: Exhausted all {} retries. Database connection is unavailable. Full exception trace below:", maxRetries, e);
                    throw new IllegalStateException("FATAL: Database connection failed after " + maxRetries + " attempts. Exact cause: " + e.getMessage(), e);
                }
                try {
                    Thread.sleep(4000); // 4-second delay
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Startup interrupted while waiting for database retry", ie);
                }
            }
            attempt++;
        }
        
        logger.info("SUCCESS: All production security and environment validations passed. Application is ready to initialize beans.");

        // Force Hikari and Hibernate to wait for us, ensuring no premature crashes
        if (beanFactory.containsBeanDefinition("dataSource")) {
            beanFactory.getBeanDefinition("dataSource").setDependsOn("configValidator");
        }
        if (beanFactory.containsBeanDefinition("entityManagerFactory")) {
            beanFactory.getBeanDefinition("entityManagerFactory").setDependsOn("configValidator");
        }
    }
}
