package com.restaurant.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * CORS Configuration for the API Gateway
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        // Allow frontend origins
        corsConfig.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:*",     // All localhost ports (Vite, Tauri dev, etc.)
                "http://127.0.0.1:*",     // Alternative localhost
                "tauri://localhost",      // Tauri production
                "tauri://*",              // Tauri any
                "ipc://localhost",        // Tauri 2.x IPC
                "https://tauri.localhost" // Tauri HTTPS variant
        ));

        // Allow all common HTTP methods
        corsConfig.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // Allow all headers
        corsConfig.setAllowedHeaders(List.of("*"));

        // Allow credentials (cookies, authorization headers)
        corsConfig.setAllowCredentials(true);

        // Expose headers to frontend
        corsConfig.setExposedHeaders(Arrays.asList(
                "Authorization",
                "X-Session-ID"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
