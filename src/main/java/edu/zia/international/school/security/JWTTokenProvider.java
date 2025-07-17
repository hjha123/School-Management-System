package edu.zia.international.school.security;

import edu.zia.international.school.exception.AppRuntimeException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Component
public class JWTTokenProvider {

    @Value("${app.jwt-secret}")
    private String jwtSecret;
    @Value("${app.jwt-expiration-milliseconds}")
    private long expirationDate;

    // Generate JWT Token
    public String getToken(Authentication authentication){
        String username = authentication.getName();
        Date currDate = new Date();

        Date expiryDate = new Date(currDate.getTime() + expirationDate);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(key())
                .compact();
    }

    private Key key(){
        return Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(jwtSecret));
    }

    // Get username from JWT token
    public String getUsername(String token){
        Claims claims = Jwts.parser()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    // Validate JWT Token
    public boolean validateToken(String token){
        try {
            Jwts.parser()
                    .verifyWith((SecretKey) key())
                    .build()
                    .parse(token);
            return true;
        } catch (MalformedJwtException malformedJwtException){
            throw new AppRuntimeException(HttpStatus.BAD_REQUEST, "Invalid JWT Token");
        } catch (ExpiredJwtException expiredJwtException){
            throw new AppRuntimeException(HttpStatus.BAD_REQUEST, "Expired JWT Token");
        } catch (UnsupportedJwtException unsupportedJwtException){
            throw new AppRuntimeException(HttpStatus.BAD_REQUEST, "Unsupported JWT token");
        } catch (IllegalArgumentException illegalArgumentException){
            throw new AppRuntimeException(HttpStatus.BAD_REQUEST, "Jwt claims string is null or empty");
        }
    }
}
