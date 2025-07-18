package smart.ai.admin.v2.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import smart.ai.admin.v2.service.AuthServiceV2;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v2/auth")
@RequiredArgsConstructor
public class AuthControllerV2 {
    
    private final AuthServiceV2 authServiceV2;
    
    @PostMapping("/login")
    public Mono<ResponseEntity<Map<String, Object>>> login(ServerWebExchange exchange, @RequestBody Map<String, Object> request) {
        String username = String.valueOf(request.get("username"));
        String password = String.valueOf(request.get("password"));
        
        if ("null".equals(username) || "null".equals(password)) {
            return Mono.just(ResponseEntity.badRequest().body(Map.of("error", "Username and password are required")));
        }
        
        log.info("Login attempt for user: {}", username);
        return authServiceV2.login(exchange, username, password);
    }
    
    @PostMapping("/logout")
    public Mono<ResponseEntity<Void>> logout(ServerWebExchange exchange) {
        log.info("Logout request");
        return authServiceV2.logout(exchange);
    }
    
    @PostMapping("/refresh")
    public Mono<ResponseEntity<Map<String, Object>>> refreshToken(ServerWebExchange exchange, @RequestBody Map<String, Object> request) {
        String refreshToken = String.valueOf(request.get("refreshToken"));
        
        if ("null".equals(refreshToken)) {
            return Mono.just(ResponseEntity.badRequest().body(Map.of("error", "Refresh token is required")));
        }
        
        log.info("Token refresh request");
        return authServiceV2.refreshToken(exchange, refreshToken);
    }
    
    @GetMapping("/me")
    public Mono<ResponseEntity<Map<String, Object>>> getCurrentUser(ServerWebExchange exchange) {
        // 현재 사용자 정보 반환 (JWT 토큰에서 추출)
        return Mono.just(ResponseEntity.ok(Map.of("message", "Current user info")));
    }
} 