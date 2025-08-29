package com.opsara.sodagent.security;

import io.jsonwebtoken.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
public class JwtUtil {
    private final String SECRET_KEY = "OPSARA_KEY_1234567890"; // Replace with a secure key
    private final long EXPIRATION = 1000 * 60 * 60; // 1 hour

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY)
                .parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }


    public List<GrantedAuthority> extractAuthorities(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY) // secretKey should be your JWT signing key
                .parseClaimsJws(token)
                .getBody();

        Object roles = claims.get("roles"); // or "authorities" depending on your token
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (roles instanceof Collection<?>) {
            for (Object role : (Collection<?>) roles) {
                authorities.add(new SimpleGrantedAuthority(role.toString()));
            }
        } else if (roles instanceof String) {
            authorities.add(new SimpleGrantedAuthority(roles.toString()));
        }
        return authorities;
    }


    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }


    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }
}