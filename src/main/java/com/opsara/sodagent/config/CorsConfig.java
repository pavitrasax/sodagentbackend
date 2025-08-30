package com.opsara.sodagent.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

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
                        "http://localhost:3000",
                        "https://d83104de-8dc1-4e95-ad1a-f2d62d63f7d6-00-1tq1p0cz1y1el.janeway.replit.dev"

                ) // Allow requests from this origin
                .allowedMethods("*") // Allow all HTTP methods (GET, POST, PUT, DELETE, etc.)
                .allowedHeaders("*") // Allow all headers
                .allowCredentials(true) // Allow sending of credentials (cookies, HTTP authentication)
                .maxAge(3600); // Cache the preflight response for 1 hour
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("https://*.replit.dev", "https://*.replit.app", "https://opsara.io", "https://d83104de-8dc1-4e95-ad1a-f2d62d63f7d6-00-1tq1p0cz1y1el.janeway.replit.dev"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}