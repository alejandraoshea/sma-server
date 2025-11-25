package com.example.telemedicine.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ActivityTrackingFilter extends OncePerRequestFilter {

    private final ActiveUserService activeUserService;
    private final JwtService jwtService;

    public ActivityTrackingFilter(ActiveUserService activeUserService, JwtService jwtService) {
        this.activeUserService = activeUserService;
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String auth = request.getHeader("Authorization");

        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            try {
                Claims claims = jwtService.extractClaims(token);
                String userId = claims.getSubject();
                activeUserService.markActive(userId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        filterChain.doFilter(request, response);
    }
}