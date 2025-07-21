package smart.ai.admin.v2.auth.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import smart.ai.admin.v2.auth.service.AuthServiceV2;

import java.util.Map;

/**
 * 인증 컨트롤러 V2 - 사용자 인증 관련 API 엔드포인트
 * 
 * 이 컨트롤러는 사용자 로그인, 로그아웃, 토큰 갱신 등의 인증 관련 기능을 제공합니다.
 * JWT 기반 인증을 사용하며, 모든 요청과 응답에 대해 상세한 로깅을 수행합니다.
 * 
 * 주요 기능:
 * - 사용자 로그인 (/api/v2/auth/login)
 * - 사용자 로그아웃 (/api/v2/auth/logout)
 * - JWT 토큰 갱신 (/api/v2/auth/refresh)
 * - 현재 사용자 정보 조회 (/api/v2/auth/me)
 * 
 * @author Yongho Kim
 * @version 2.0
 * @since 2025
 */
@Slf4j
@RestController
@RequestMapping("/api/v2/auth")
@RequiredArgsConstructor
public class AuthControllerV2 {
    
    private final AuthServiceV2 authServiceV2;
    
    /**
     * 사용자 로그인 API
     * 
     * 사용자명과 비밀번호를 받아 인증을 수행하고, 성공 시 JWT 토큰을 반환합니다.
     * 응답에는 accessToken, refreshToken, 사용자 정보가 포함됩니다.
     * 
     * @param exchange ServerWebExchange 객체 (요청/응답 컨텍스트)
     * @param request 로그인 요청 데이터 (username, password)
     * @return 로그인 결과 (성공 시 토큰 포함, 실패 시 에러 메시지)
     */
    @PostMapping("/login")
    public Mono<ResponseEntity<Map<String, Object>>> login(ServerWebExchange exchange, @RequestBody Map<String, Object> request) {
        String username = String.valueOf(request.get("username"));
        String password = String.valueOf(request.get("password"));
        
        log.info("[LOGIN] API 호출 시작 - 사용자명: {}", username);
        log.debug("[LOGIN] 요청 데이터: {}", request);
        
        if ("null".equals(username) || "null".equals(password)) {
            log.warn("[LOGIN] 실패 - 사용자명 또는 비밀번호 누락 - 사용자명: {}", username);
            return Mono.just(ResponseEntity.badRequest().body(Map.of("error", "Username and password are required")));
        }
        
        return authServiceV2.login(exchange, username, password)
                .doOnSuccess(response -> {
                    if (response.getStatusCode().is2xxSuccessful()) {
                        log.info("[LOGIN] 성공 - HTTP {} - 사용자명: {}", response.getStatusCodeValue(), username);
                    } else {
                        log.warn("[LOGIN] 실패 - HTTP {} - 사용자명: {} - 응답: {}", 
                                response.getStatusCodeValue(), username, response.getBody());
                    }
                })
                .doOnError(error -> {
                    log.error("[LOGIN] 예외 발생 - 사용자명: {} - 오류: {}", username, error.getMessage());
                });
    }
    
    /**
     * 사용자 로그아웃 API
     * 
     * 현재 사용자의 세션을 종료하고 JWT 토큰을 무효화합니다.
     * 
     * @param exchange ServerWebExchange 객체
     * @return 로그아웃 결과
     */
    @PostMapping("/logout")
    public Mono<ResponseEntity<Void>> logout(ServerWebExchange exchange) {
        log.info("=== 로그아웃 API 호출 시작 ===");
        log.info("로그아웃 요청 처리 중...");
        return authServiceV2.logout(exchange)
                .doOnSuccess(response -> {
                    log.info("=== 로그아웃 API 호출 성공 - HTTP {} ===", response.getStatusCodeValue());
                });
    }
    
    /**
     * JWT 토큰 갱신 API
     * 
     * 만료된 accessToken을 refreshToken을 사용하여 갱신합니다.
     * 
     * @param exchange ServerWebExchange 객체
     * @param request 토큰 갱신 요청 데이터 (refreshToken)
     * @return 새로운 accessToken과 refreshToken
     */
    @PostMapping("/refresh")
    public Mono<ResponseEntity<Map<String, Object>>> refreshToken(ServerWebExchange exchange, @RequestBody Map<String, Object> request) {
        String refreshToken = String.valueOf(request.get("refreshToken"));
        
        log.info("=== 토큰 갱신 API 호출 시작 ===");
        log.info("토큰 갱신 요청 처리 중...");
        
        if ("null".equals(refreshToken)) {
            log.warn("토큰 갱신 실패: 리프레시 토큰 누락");
            log.info("=== 토큰 갱신 API 호출 실패 - HTTP 400 ===");
            return Mono.just(ResponseEntity.badRequest().body(Map.of("error", "Refresh token is required")));
        }
        
        return authServiceV2.refreshToken(exchange, refreshToken)
                .doOnSuccess(response -> {
                    if (response.getStatusCode().is2xxSuccessful()) {
                        log.info("=== 토큰 갱신 API 호출 성공 - HTTP {} ===", response.getStatusCodeValue());
                    } else {
                        log.warn("=== 토큰 갱신 API 호출 실패 - HTTP {} ===", response.getStatusCodeValue());
                    }
                });
    }
    
    /**
     * 현재 사용자 정보 조회 API
     * 
     * JWT 토큰에서 추출한 현재 사용자의 정보를 반환합니다.
     * 
     * @param exchange ServerWebExchange 객체
     * @return 현재 사용자 정보
     */
    @GetMapping("/me")
    public Mono<ResponseEntity<Map<String, Object>>> getCurrentUser(ServerWebExchange exchange) {
        log.info("=== 현재 사용자 정보 조회 API 호출 시작 ===");
        
        // JWT 필터에서 설정한 사용자 정보 가져오기
        String username = exchange.getAttribute("username");
        Long userId = exchange.getAttribute("userId");
        
        if (username == null || userId == null) {
            log.warn("사용자 정보 없음 - 토큰에서 추출 실패");
            log.info("=== 현재 사용자 정보 조회 API 호출 실패 - HTTP 401 ===");
            return Mono.just(ResponseEntity.status(401).body(Map.of("error", "User information not found")));
        }
        
        log.info("현재 사용자 정보 조회: 사용자={}, ID={}", username, userId);
        log.info("=== 현재 사용자 정보 조회 API 호출 성공 - HTTP 200 ===");
        
        return Mono.just(ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Current user info retrieved successfully",
            "user", Map.of(
                "id", userId,
                "username", username
            )
        )));
    }
} 