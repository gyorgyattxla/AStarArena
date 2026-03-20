package com.astar.arena.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final String JWT_SECRET = "8z#Lp29!vN_qS4&mK7*tX1@yB6^rJ5%w";
    private static final long JWT_EXPIRATION = 86400000L;

    private final Key key = Keys.hmacShaKeyFor( JWT_SECRET.getBytes() );

    public String generateToken( String email ) {
        Date now = new Date();
        Date expiryDate = new Date( now.getTime() + JWT_EXPIRATION );

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken( String token ) {
        try{
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch ( JwtException | IllegalArgumentException e ) {
            // TODO: Log error
            return false;
        }
    }

    public String getEmailFromToken( String token ) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

}
