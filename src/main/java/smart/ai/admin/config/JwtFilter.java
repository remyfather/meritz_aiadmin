package smart.ai.admin.config;

import java.util.Arrays;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;
import smart.ai.admin.service.auth.AuthService;
import smart.ai.admin.util.JwtUtil;

@Component
public class JwtFilter implements WebFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    AuthService authService;

    @Override
    @SuppressWarnings("null")
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String path = exchange.getRequest().getPath().value();

        // 인증 bypass용 suffix 목록
        String[] suffix = { ".js", ".css", ".ico", ".map" };
        if (!path.startsWith("/api")) {
            if (Arrays.stream(suffix).anyMatch(path::endsWith) || path.equals("/login")) {
                return chain.filter(exchange);
            }
        }

        // auth api면 jwt 인증 pass
        if (path.startsWith("/api/auth")) {
            return chain.filter(exchange);
        }

        // access토큰 가져오기(from client request)
        Optional<String> accessToken = jwtUtil.getAccessToken(request);
        // access토큰 없을시 토큰 refresh 진행, refresh토큰도 없을시 401 에러
        if (accessToken.isEmpty()) {
            Optional<String> refreshToken = jwtUtil.getRefreshToken(request);
            if (refreshToken.isEmpty()) {
                log.warn("Missing JWT token for path: {}", path);
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return chain.filter(exchange);
            }
            return authService.refreshService(exchange)
                    .flatMap(res -> {
                        // access토큰 가져오기(from refresh response)
                        Optional<String> newToken = jwtUtil.getAccessToken(response);
                        return this.setUserData(exchange, newToken);
                    })
                    .flatMap(statusCode -> {
                        if (!statusCode.is2xxSuccessful()) {
                            response.setStatusCode(statusCode);
                        }
                        return chain.filter(exchange);
                    });
        }
        return this.setUserData(exchange, accessToken)
                .flatMap(statusCode -> {
                    if (!statusCode.is2xxSuccessful()) {
                        response.setStatusCode(statusCode);
                        return chain.filter(exchange);
                    }
                    return jwtUtil.verifyJwt(accessToken.get())
                            .flatMap(res -> chain.filter(exchange))
                            .onErrorResume(WebClientResponseException.class, e -> {
                                response.setStatusCode(e.getStatusCode());
                                try {
                                    byte[] bytes = e.getResponseBodyAsString().getBytes();
                                    return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
                                } catch (Exception ex) {
                                    log.warn(ex.toString());
                                }
                                return chain.filter(exchange);
                            })
                            .onErrorResume(Exception.class, e -> {
                                log.error("An unexpected error occurred during JWT verification: {}", e.toString());
                                response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                                return chain.filter(exchange);
                            });
                });
    }

    // 사용자 정보 exchange에 저장
    private Mono<HttpStatus> setUserData(ServerWebExchange exchange, Optional<String> tokenOptional) {
        String path = exchange.getRequest().getPath().value();
        // 토큰이 없을 시 401 에러
        if (tokenOptional.isEmpty()) {
            return Mono.just(HttpStatus.UNAUTHORIZED);
        }

        String token = tokenOptional.get();
        // access토큰 내 정보 가져오기
        Optional<String> userRole = jwtUtil.getPayloadValue(token, "userAuthCd");
        if (userRole.isEmpty()) {
            return Mono.just(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        Optional<String> userName = jwtUtil.getPayloadValue(token, "userNm");
        if (userName.isEmpty()) {
            return Mono.just(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        exchange.getAttributes().put("userRole", userRole.get());
        exchange.getAttributes().put("userName", userName.get());

        // admin유저가 아니고 admin api호출시 500에러 반환
        if (!"SAU".equals(userRole.get()) && path.startsWith("/api/admin")) {
            return Mono.just(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return Mono.just(HttpStatus.OK);
    }
}