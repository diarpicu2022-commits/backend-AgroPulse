package com.agropulse.config;

import com.agropulse.security.JwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(openCorsConfig()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Auth endpoints — public
                .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/sync-google-user").permitAll()
                // Admin user management — requires ADMIN role from JWT
                .requestMatchers(HttpMethod.GET,  "/auth/users").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,  "/auth/users/*/role").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,  "/auth/users/*/greenhouses").hasRole("ADMIN")
                // User CRUD — admin only (greenhouse lookup stays open for auth flow)
                .requestMatchers(HttpMethod.GET,  "/users").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/users").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,  "/users/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/users/*").hasRole("ADMIN")
                // Firmware OTA — upload restringido a ADMIN; download/version via device-code en controller
                .requestMatchers(HttpMethod.POST, "/firmware/upload").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET,  "/firmware/version").permitAll()
                .requestMatchers(HttpMethod.GET,  "/firmware/download").permitAll()
                .requestMatchers(HttpMethod.GET,  "/firmware/signature").permitAll()
                // System settings — admin only
                .requestMatchers(HttpMethod.GET, "/api/system-settings").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/system-settings/*").hasRole("ADMIN")
                // Everything else (device endpoints, readings, etc.) — open
                .anyRequest().permitAll()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private CorsConfigurationSource openCorsConfig() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
