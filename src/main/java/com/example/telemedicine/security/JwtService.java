package com.example.telemedicine.security;

import com.example.telemedicine.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

/**
 * Service class responsible for creating and validating JSON Web Tokens (JWTs)
 * for the Telemedicine application to securely transmit information between the
 * frontend and backend. This service handles token generation and extracting
 * claims from existing tokens.
 */
@Service
public class JwtService {

    /**
     * Secret key found in application-local.yml used to sign and validate JWT tokens.
     */
    @Value("${jwt.secret}")
    private String secretKey;

    /**
     * Expiration time found application-local.yml for JWT tokens in milliseconds.
     */
    @Value("${jwt.expiration}")
    private long expirationMs;

    /**
     * Returns a {@link Key} object constructed from the Base64-encoded secret key.
     * This key is used to sign and validate JWT tokens.
     *
     * @return Key used for signing and validating JWTs
     */
    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generates a signed JWT token for the given {@link User}.
     *
     * @param user The authenticated user for whom the token is being generated
     * @return A signed JWT token as a {@link String}
     */
    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("role", user.getRole())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSignKey())
                .compact();
    }

    /**
     * Extracts the claims (payload data) from a given JWT token. Validates the
     * token signature and throws an error if it is invalid or expired.
     *
     * @param token The JWT token string to parse
     * @return {@link Claims} object containing the token's payload data
     */
    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}