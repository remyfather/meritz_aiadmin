package smart.ai.admin.v2.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import smart.ai.admin.v2.common.domain.User;
import smart.ai.admin.v2.common.repository.UserRepository;
import smart.ai.admin.v2.common.util.JwtUtilV2;

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
            log.info("[AUTH] 로그인 시도 - 사용자명: {}", username);
            
            // 사용자 조회
            User user = userRepository.findByUsername(username)
                    .orElse(null);
            
            if (user == null) {
                log.warn("[AUTH] 사용자 없음 - 사용자명: {}", username);
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "error", "사용자를 찾을 수 없습니다",
                    "errorCode", "USER_NOT_FOUND"
                ));
            }
            
            // 비밀번호 검증
            if (!user.checkPassword(password)) {
                log.warn("[AUTH] 잘못된 비밀번호 - 사용자명: {}", username);
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "error", "비밀번호가 올바르지 않습니다",
                    "errorCode", "INVALID_PASSWORD"
                ));
            }
            
            // 계정 상태 확인
            if (!user.isEnabled()) {
                log.warn("[AUTH] 비활성 계정 - 사용자명: {}", username);
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "error", "비활성화된 계정입니다. 관리자에게 문의하세요",
                    "errorCode", "ACCOUNT_DISABLED"
                ));
            }
            
            // JWT 토큰 생성
            String accessToken = jwtUtilV2.generateAccessToken(user);
            String refreshToken = jwtUtilV2.generateRefreshToken(user);
            
            // 마지막 로그인 시간 업데이트
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
            
            log.info("[AUTH] 로그인 성공 - 사용자: {} (역할: {})", username, user.getRole().getName());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "로그인에 성공했습니다");
            result.put("accessToken", accessToken);
            result.put("refreshToken", refreshToken);
            result.put("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "name", user.getName(),
                "email", user.getEmail(),
                "role", user.getRole().getName()
            ));
            
            return ResponseEntity.ok(result);
        });
    }
    
    public Mono<ResponseEntity<Void>> logout(ServerWebExchange exchange) {
        return Mono.fromCallable(() -> {
            log.info("=== 로그아웃 트랜잭션 시작 ===");
            log.info("로그아웃 요청 처리 중...");
            log.info("=== 로그아웃 트랜잭션 완료 ===");
            return ResponseEntity.ok().<Void>build();
        });
    }
    
    public Mono<ResponseEntity<Map<String, Object>>> refreshToken(ServerWebExchange exchange, String refreshToken) {
        return Mono.fromCallable(() -> {
            log.info("=== 토큰 갱신 트랜잭션 시작 ===");
            log.info("토큰 갱신 요청 처리 중...");
            
            if (!jwtUtilV2.validateToken(refreshToken)) {
                log.warn("토큰 갱신 실패: 유효하지 않은 리프레시 토큰");
                log.info("=== 토큰 갱신 트랜잭션 실패 (유효하지 않은 토큰) ===");
                return ResponseEntity.status(401).body(Map.of("error", "Invalid refresh token"));
            }
            
            String username = jwtUtilV2.getUsernameFromToken(refreshToken);
            User user = userRepository.findByUsername(username).orElse(null);
            
            if (user == null) {
                log.warn("토큰 갱신 실패: 사용자를 찾을 수 없음 - {}", username);
                log.info("=== 토큰 갱신 트랜잭션 실패 (사용자 없음) ===");
                return ResponseEntity.status(401).body(Map.of("error", "User not found"));
            }
            
            // 새로운 토큰 생성
            String newAccessToken = jwtUtilV2.generateAccessToken(user);
            String newRefreshToken = jwtUtilV2.generateRefreshToken(user);
            
            log.info("토큰 갱신 성공: 사용자={}", username);
            log.info("=== 토큰 갱신 트랜잭션 성공 ===");
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Token refreshed successfully");
            result.put("accessToken", newAccessToken);
            result.put("refreshToken", newRefreshToken);
            
            return ResponseEntity.ok(result);
        });
    }
} 