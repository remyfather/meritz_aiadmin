package smart.ai.admin.v2.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import smart.ai.admin.v2.domain.User;
import smart.ai.admin.v2.repository.UserRepository;
import smart.ai.admin.v2.util.JwtUtilV2;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceV2 {
    
    private final UserRepository userRepository;
    private final JwtUtilV2 jwtUtilV2;
    
    public Mono<ResponseEntity<Map<String, Object>>> login(ServerWebExchange exchange, String username, String password) {
        return Mono.fromCallable(() -> {
            // 사용자 조회
            User user = userRepository.findByUsername(username)
                    .orElse(null);
            
            if (user == null) {
                log.warn("Login failed: User not found - {}", username);
                return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
            }
            
            // 비밀번호 검증
            if (!user.checkPassword(password)) {
                log.warn("Login failed: Invalid password for user - {}", username);
                return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
            }
            
            // 계정 상태 확인
            if (user.getStatus() != User.UserStatus.ACTIVE) {
                log.warn("Login failed: Inactive account - {}", username);
                return ResponseEntity.status(401).body(Map.of("error", "Account is not active"));
            }
            
            // JWT 토큰 생성
            String accessToken = jwtUtilV2.generateAccessToken(user);
            String refreshToken = jwtUtilV2.generateRefreshToken(user);
            
            // 쿠키 설정
            ServerHttpResponse response = exchange.getResponse();
            ResponseCookie accessTokenCookie = ResponseCookie.from("access", accessToken)
                    .maxAge(Duration.ofMinutes(45))
                    .path("/")
                    .httpOnly(true)
                    .build();
            response.addCookie(accessTokenCookie);
            
            ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh", refreshToken)
                    .maxAge(Duration.ofMinutes(110))
                    .path("/")
                    .httpOnly(true)
                    .build();
            response.addCookie(refreshTokenCookie);
            
            // 마지막 로그인 시간 업데이트
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
            
            log.info("Login successful: {}", username);
            
            Map<String, Object> result = new HashMap<>();
            result.put("message", "Login successful");
            result.put("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "name", user.getName(),
                "email", user.getEmail()
            ));
            
            return ResponseEntity.ok(result);
        });
    }
    
    public Mono<ResponseEntity<Void>> logout(ServerWebExchange exchange) {
        return Mono.fromCallable(() -> {
            ServerHttpResponse response = exchange.getResponse();
            
            // 쿠키 삭제
            ResponseCookie accessTokenCookie = ResponseCookie.from("access", "")
                    .maxAge(0)
                    .path("/")
                    .httpOnly(true)
                    .build();
            response.addCookie(accessTokenCookie);
            
            ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh", "")
                    .maxAge(0)
                    .path("/")
                    .httpOnly(true)
                    .build();
            response.addCookie(refreshTokenCookie);
            
            log.info("Logout successful");
            return ResponseEntity.noContent().<Void>build();
        });
    }
    
    public Mono<ResponseEntity<Map<String, Object>>> refreshToken(ServerWebExchange exchange, String refreshToken) {
        return Mono.fromCallable(() -> {
            if (!jwtUtilV2.validateToken(refreshToken)) {
                log.warn("Token refresh failed: Invalid refresh token");
                return ResponseEntity.status(401).body(Map.of("error", "Invalid refresh token"));
            }
            
            String username = jwtUtilV2.getUsernameFromToken(refreshToken);
            User user = userRepository.findByUsername(username).orElse(null);
            
            if (user == null) {
                log.warn("Token refresh failed: User not found - {}", username);
                return ResponseEntity.status(401).body(Map.of("error", "User not found"));
            }
            
            // 새로운 토큰 생성
            String newAccessToken = jwtUtilV2.generateAccessToken(user);
            String newRefreshToken = jwtUtilV2.generateRefreshToken(user);
            
            // 쿠키 업데이트
            ServerHttpResponse response = exchange.getResponse();
            ResponseCookie accessTokenCookie = ResponseCookie.from("access", newAccessToken)
                    .maxAge(Duration.ofMinutes(45))
                    .path("/")
                    .httpOnly(true)
                    .build();
            response.addCookie(accessTokenCookie);
            
            ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh", newRefreshToken)
                    .maxAge(Duration.ofMinutes(110))
                    .path("/")
                    .httpOnly(true)
                    .build();
            response.addCookie(refreshTokenCookie);
            
            log.info("Token refresh successful: {}", username);
            
            Map<String, Object> result = new HashMap<>();
            result.put("message", "Token refreshed successfully");
            
            return ResponseEntity.ok(result);
        });
    }
} 