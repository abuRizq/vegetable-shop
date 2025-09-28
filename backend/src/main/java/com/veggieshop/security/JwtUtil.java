package com.veggieshop.security;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.function.Function;

/**
 * Utility class for creating and validating JWT tokens.
 * Only used for Access Tokens (short-lived).
 */
@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    /**
     * Generates a JWT access token for the given UserDetails.
     */
    public String generateAccessToken(UserDetails userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    /**
     * Retrieves the username (subject) from the JWT token.
     * @throws ExpiredJwtException if token is expired
     * @throws JwtException if token is invalid
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * Validates the JWT token for the given UserDetails and checks expiration.
     * @return true if valid; false if expired or username mismatch.
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = getUsernameFromToken(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (ExpiredJwtException e) {
            // Token expired
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            // Invalid token
            return false;
        }
    }

    /**
     * Checks if the JWT token is expired.
     * @return true if expired; false if still valid.
     * @throws JwtException if the token is invalid (malformed, tampered, etc.)
     */
    public boolean isTokenExpired(String token) {
        try {
            final Date expiration = getClaimFromToken(token, Claims::getExpiration);
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            // If parsing throws ExpiredJwtException, consider it expired
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // If token is invalid, escalate the error (let caller handle)
            throw e;
        }
    }

    /**
     * Extracts a specific claim from the JWT token.
     * @throws ExpiredJwtException if token is expired
     * @throws JwtException if token is invalid
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = parseToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parses the JWT token and returns all claims.
     * @throws ExpiredJwtException if token is expired
     * @throws JwtException if token is invalid
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();
    }
}
