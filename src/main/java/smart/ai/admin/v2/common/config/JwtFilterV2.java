package smart.ai.admin.v2.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import smart.ai.admin.v2.common.util.JwtUtilV2;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilterV2 implements WebFilter {
    
    private final JwtUtilV2 jwtUtilV2;
    
    // 인증이 필요하지 않은 경로들
    private static final List<String> PUBLIC_PATHS = List.of(
        "/",
        "/api/v2/auth/login",
        "/api/v2/auth/signup",
        "/api/v2/admin/super-admin",
        "/login",
        "/admin/users",
        "/css/",
        "/js/",
        "/lib/",
        "/favicon.ico"
    );
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod().name();
        
        // 공개 경로는 인증 없이 통과
        if (isPublicPath(path)) {
            log.debug("공개 경로 접근: {} {}", method, path);
            return chain.filter(exchange);
        }
        
        log.info("=== JWT 인증 필터 시작 ===");
        log.info("보호된 경로 접근: {} {}", method, path);
        
        // Authorization 헤더에서 토큰 추출
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("인증 헤더 누락 또는 형식 오류: {} {} - 헤더: {}", method, path, authHeader);
            log.info("=== JWT 인증 필터 실패 (헤더 오류) - HTTP 401 ===");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        
        String token = authHeader.substring(7); // "Bearer " 제거
        log.debug("JWT 토큰 추출 완료: {} {}", method, path);
        
        try {
            // 토큰 검증
            if (!jwtUtilV2.validateToken(token)) {
                log.warn("유효하지 않은 JWT 토큰: {} {} - 토큰: {}", method, path, token.substring(0, Math.min(20, token.length())) + "...");
                log.info("=== JWT 인증 필터 실패 (토큰 검증 실패) - HTTP 401 ===");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
            
            // 토큰에서 사용자 정보 추출하여 요청 속성에 추가
            String username = jwtUtilV2.getUsernameFromToken(token);
            Long userId = jwtUtilV2.getUserIdFromToken(token);
            
            exchange.getAttributes().put("username", username);
            exchange.getAttributes().put("userId", userId);
            
            log.info("JWT 인증 성공: {} {} - 사용자={}, ID={}", method, path, username, userId);
            log.info("=== JWT 인증 필터 성공 - HTTP 200 ===");
            
        } catch (Exception e) {
            log.warn("JWT 토큰 검증 중 오류 발생: {} {} - 오류={}", method, path, e.getMessage());
            log.info("=== JWT 인증 필터 실패 (예외 발생) - HTTP 401 ===");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        
        return chain.filter(exchange);
    }
    
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(publicPath -> {
            if (publicPath.endsWith("/")) {
                return path.startsWith(publicPath);
            } else {
                return path.equals(publicPath);
            }
        });
    }
} 