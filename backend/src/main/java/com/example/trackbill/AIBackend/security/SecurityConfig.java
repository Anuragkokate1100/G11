package com.example.trackbill.AIBackend.security;

import com.example.trackbill.AIBackend.model.TractorOwner;
import com.example.trackbill.AIBackend.repository.TractorOwnerRepository;
import com.example.trackbill.AIBackend.repository.FarmerRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final TractorOwnerRepository ownerRepository;

    @Value("${app.cors.allowed-origins:http://localhost:8080,http://127.0.0.1:8080,http://localhost:5500,http://127.0.0.1:5500}")
    private String allowedOrigins;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, TractorOwnerRepository ownerRepository) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.ownerRepository = ownerRepository;
    }

    /**
     * Use registered tractor-owner accounts for Spring Security's user lookup.
     * The application login endpoint still issues JWTs through AuthService.
     * Defining this bean prevents Spring Boot from creating a temporary
     * in-memory user and printing a generated development password at startup.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return email -> {
            TractorOwner owner = ownerRepository.findByEmail(email.trim().toLowerCase())
                    .orElseThrow(() -> new UsernameNotFoundException("Invalid email or password"));

            return User.withUsername(owner.getEmail())
                    .password(owner.getPassword())
                    .roles("TRACTOR_OWNER")
                    .build();
        };
    }

    /**
     * JwtAuthenticationFilter is added to Spring Security's filter chain below.
     * Disable its separate servlet-container registration to avoid it running
     * twice and interfering with SecurityContext handling.
     */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterServletRegistration(
            JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration =
                new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {
                    response.setContentType("application/json");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"success\":false,\"message\":\"Unauthorized: Please login to access this resource\"}");
                }))
                .authorizeHttpRequests(auth -> auth
                        // Public Auth APIs
                        .requestMatchers("/api/auth/**").permitAll()
                        // Public Static frontend files
                        .requestMatchers(
                                "/",
                                "/*.html",
                                "/index.html",
                                "/tractorlogin.html",
                                "/register.html",
                                "/registration.html",
                                "/tractorhome.html",
                                "/farmerhome.html",
                                "/farmer-register.html",
                                "/labour.html",
                                "/trips.html",
                                "/expenses.html",
                                "/billing.html",
                                "/reports.html",
                                "/profile.html",
                                "/ai-assistant.html",
                                "/css/**",
                                "/js/**",
                                "/assets/**",
                                "/favicon.ico",
                                "/error"
                        ).permitAll()
                        // Preflight OPTIONS requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Farmer APIs are isolated from owner APIs.
                        .requestMatchers("/api/farmer/**").hasRole("FARMER")
                        // Existing APIs remain owner-only.
                        .requestMatchers("/api/**").hasRole("TRACTOR_OWNER")
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        List<String> origins = new ArrayList<>();
        if (allowedOrigins != null && !allowedOrigins.isBlank()) {
            for (String origin : allowedOrigins.split(",")) {
                if (!origin.trim().isEmpty()) {
                    origins.add(origin.trim());
                }
            }
        }
        if (origins.isEmpty()) {
            origins = List.of(
                    "http://localhost:8080", "http://127.0.0.1:8080",
                    "http://localhost:5500", "http://127.0.0.1:5500",
                    "http://localhost:3000", "http://127.0.0.1:3000"
            );
        }
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
