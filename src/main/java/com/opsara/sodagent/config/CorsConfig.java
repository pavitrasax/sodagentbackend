package com.opsara.sodagent.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Apply CORS to all paths
                .allowedOriginPatterns(
                        "http://localhost:3001",
                        "https://opsara.io",
                        "https://www.opsara.io",
                        "https://*.replit.app",
                        "https://replit.app",
                        "https://*.replit.dev",
                        "https://replit.dev",
                        "http://localhost:3000"
                ) // Allow requests from this origin
                .allowedMethods("*") // Allow all HTTP methods (GET, POST, PUT, DELETE, etc.)
                .allowedHeaders("*") // Allow all headers
                .allowCredentials(true) // Allow sending of credentials (cookies, HTTP authentication)
                .maxAge(3600); // Cache the preflight response for 1 hour
    }
}