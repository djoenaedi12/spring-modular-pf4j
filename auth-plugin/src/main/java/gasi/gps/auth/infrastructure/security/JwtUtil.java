package gasi.gps.auth.infrastructure.security;

import java.util.Date;
import java.util.Set;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * Utility class for JWT token generation and validation.
 */
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    /**
     * Constructs JwtUtil with configurable secret and expiration values.
     */
    public JwtUtil(
            @Value("${auth.jwt.secret:dGhpcyBpcyBhIHZlcnkgc2VjcmV0IGtleSBmb3Igand0IGF1dGg=}") String secret,
            @Value("${auth.jwt.access-token-expiration:3600}") long accessTokenExpiration,
            @Value("${auth.jwt.refresh-token-expiration:86400}") long refreshTokenExpiration) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    /**
     * Generate an access token with a specific JTI.
     */
    public String generateAccessTokenWithJti(String username,
            Set<String> roles, Set<String> scopes, long expirationSeconds, String jti) {
        var builder = Jwts.builder()
                .id(jti)
                .claim("username", username)
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationSeconds * 1000))
                .signWith(key);

        if (scopes != null && !scopes.isEmpty()) {
            builder.claim("scopes", scopes);
        }

        return builder.compact();
    }

    /**
     * Generate a refresh token with a specific JTI.
     */
    public String generateRefreshTokenWithJti(long expirationSeconds, String jti) {
        return Jwts.builder()
                .id(jti)
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationSeconds * 1000))
                .signWith(key)
                .compact();
    }

    /**
     * Validate a token.
     *
     * @param token the JWT token
     * @return true if valid
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Extract all claims from a token.
     */
    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Get the user ID from a token.
     */
    public String getUserIdFromToken(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Get the username from a token.
     */
    public String getUsernameFromToken(String token) {
        return getClaims(token).get("username", String.class);
    }

    /**
     * Get the JWT ID (jti) from a token.
     */
    public String getJtiFromToken(String token) {
        return getClaims(token).getId();
    }

    /**
     * Get access token expiration in seconds.
     */
    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    /**
     * Get refresh token expiration in seconds.
     */
    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }
}
