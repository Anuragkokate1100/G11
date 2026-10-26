package com.example.trackbill.AIBackend.security;

import com.example.trackbill.AIBackend.model.Farmer;
import com.example.trackbill.AIBackend.model.TractorOwner;
import com.example.trackbill.AIBackend.repository.FarmerRepository;
import com.example.trackbill.AIBackend.repository.TractorOwnerRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final TractorOwnerRepository ownerRepository;
    private final FarmerRepository farmerRepository;
    public JwtAuthenticationFilter(JwtUtil jwtUtil, TractorOwnerRepository ownerRepository, FarmerRepository farmerRepository) {
        this.jwtUtil = jwtUtil; this.ownerRepository = ownerRepository; this.farmerRepository = farmerRepository;
    }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);
            if (jwt != null && jwtUtil.validateToken(jwt) && SecurityContextHolder.getContext().getAuthentication() == null) {
                String email = jwtUtil.extractEmail(jwt);
                String role = jwtUtil.extractRole(jwt);
                // Existing owner JWTs did not contain a role claim; preserve compatibility.
                if (role == null) role = ownerRepository.findByEmail(email).isPresent() ? "TRACTOR_OWNER" : null;
                if ("FARMER".equals(role)) {
                    Farmer farmer = farmerRepository.findByEmail(email).orElse(null);
                    if (farmer != null) authenticate(request, farmer, "ROLE_FARMER");
                } else if ("TRACTOR_OWNER".equals(role)) {
                    TractorOwner owner = ownerRepository.findByEmail(email).orElse(null);
                    if (owner != null) authenticate(request, owner, "ROLE_TRACTOR_OWNER");
                }
            }
        } catch (Exception e) { logger.error("Cannot set user authentication in SecurityContext", e); }
        filterChain.doFilter(request, response);
    }
    private void authenticate(HttpServletRequest request, Object principal, String authority) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(principal, null, Collections.singletonList(new SimpleGrantedAuthority(authority)));
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        return StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ") ? headerAuth.substring(7) : null;
    }
}
