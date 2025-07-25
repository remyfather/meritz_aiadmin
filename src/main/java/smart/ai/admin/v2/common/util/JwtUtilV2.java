package smart.ai.admin.v2.common.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import smart.ai.admin.v2.common.domain.User;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtUtilV2 {
    
    @Value("${smart.ai.jwt.secret}")
    private String jwtSecret;
    
    @Value("${smart.ai.access-token.duration}")
    private int accessTokenDuration;
    
    @Value("${smart.ai.refresh-token.duration}")
    private int refreshTokenDuration;
    
    private SecretKey getSigningKey() {
        // HS512 알고리즘을 위한 최소 512비트(64바이트) 키 생성
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 64) {
            // 키가 너무 짧으면 패딩
            byte[] paddedKey = new byte[64];
            System.arraycopy(keyBytes, 0, paddedKey, 0, Math.min(keyBytes.length, 64));
            return Keys.hmacShaKeyFor(paddedKey);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    public String generateAccessToken(User user) {
        return generateToken(user, accessTokenDuration * 60 * 1000L); // minutes to milliseconds
    }
    
    public String generateRefreshToken(User user) {
        return generateToken(user, refreshTokenDuration * 60 * 1000L); // minutes to milliseconds
    }
    
    private String generateToken(User user, long expirationTime) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("name", user.getName());
        claims.put("email", user.getEmail());
        claims.put("role", user.getRole().getName());
        
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }
    
    public Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            log.warn("JWT token is unsupported: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            log.warn("JWT token is malformed: {}", e.getMessage());
            throw e;
        } catch (SecurityException e) {
            log.warn("JWT token has invalid signature: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            log.warn("JWT token is empty: {}", e.getMessage());
            throw e;
        }
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }
    
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }
    
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }
    
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }
} 