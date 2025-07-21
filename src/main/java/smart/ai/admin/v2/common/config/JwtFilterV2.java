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
import smart.ai.admin.v2.common.repository.UserRepository;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilterV2 implements WebFilter {
    
    private final JwtUtilV2 jwtUtilV2;
    private final UserRepository userRepository;
    
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
            if (path.equals("/api/v2/auth/login")) {
                log.info("로그인 API 접근: {} {}", method, path);
            } else {
                log.debug("공개 경로 접근: {} {}", method, path);
            }
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
            log.debug("토큰 검증 시작: {}", token.substring(0, Math.min(50, token.length())) + "...");
            if (!jwtUtilV2.validateToken(token)) {
                log.warn("유효하지 않은 JWT 토큰: {} {} - 토큰: {}", method, path, token.substring(0, Math.min(20, token.length())) + "...");
                log.info("=== JWT 인증 필터 실패 (토큰 검증 실패) - HTTP 401 ===");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
            log.debug("토큰 검증 성공");
            
            // 토큰에서 사용자 정보 추출
            log.debug("사용자 정보 추출 시작");
            String username = jwtUtilV2.getUsernameFromToken(token);
            Long userId = jwtUtilV2.getUserIdFromToken(token);
            
            log.debug("토큰에서 추출한 사용자 정보: username={}, userId={}", username, userId);
            
            if (username == null || userId == null) {
                log.warn("토큰에서 사용자 정보 추출 실패: username={}, userId={}", username, userId);
                log.info("=== JWT 인증 필터 실패 (사용자 정보 추출 실패) - HTTP 401 ===");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
            
            // 사용자가 실제로 데이터베이스에 존재하는지 확인
            log.debug("DB에서 사용자 존재 확인: userId={}", userId);
            if (!userRepository.existsById(userId)) {
                log.warn("삭제된 사용자의 토큰: {} {} - 사용자={}, ID={}", method, path, username, userId);
                log.info("=== JWT 인증 필터 실패 (삭제된 사용자) - HTTP 401 ===");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                exchange.getResponse().getHeaders().add("X-User-Deleted", "true");
                return exchange.getResponse().setComplete();
            }
            log.debug("DB에서 사용자 존재 확인 성공");
            
            // 사용자 정보를 요청 속성에 추가
            exchange.getAttributes().put("username", username);
            exchange.getAttributes().put("userId", userId);
            
            log.info("JWT 인증 성공: {} {} - 사용자={}, ID={}", method, path, username, userId);
            log.info("=== JWT 인증 필터 성공 - HTTP 200 ===");
            
            return chain.filter(exchange);
            
        } catch (Exception e) {
            log.warn("JWT 토큰 검증 중 오류 발생: {} {} - 오류={}", method, path, e.getMessage());
            e.printStackTrace(); // 스택 트레이스 출력
            log.info("=== JWT 인증 필터 실패 (예외 발생) - HTTP 401 ===");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }
    
    private boolean isPublicPath(String path) {
        log.debug("isPublicPath 체크: path={}", path);
        boolean isPublic = PUBLIC_PATHS.stream().anyMatch(publicPath -> {
            boolean matches;
            if (publicPath.equals("/")) {
                // 루트 경로는 정확히 "/"인 경우만 매칭
                matches = path.equals("/");
            } else if (publicPath.endsWith("/")) {
                // 디렉토리 경로는 startsWith로 매칭
                matches = path.startsWith(publicPath);
            } else {
                // 정확한 경로 매칭
                matches = path.equals(publicPath);
            }
            if (matches) {
                log.debug("공개 경로 매칭: path={} matches publicPath={}", path, publicPath);
            }
            return matches;
        });
        log.debug("isPublicPath 결과: path={}, isPublic={}", path, isPublic);
        return isPublic;
    }
} 