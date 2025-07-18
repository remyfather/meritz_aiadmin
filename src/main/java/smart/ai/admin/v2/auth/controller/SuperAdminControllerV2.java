package smart.ai.admin.v2.auth.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import smart.ai.admin.v2.auth.dto.SuperAdminRequest;
import smart.ai.admin.v2.auth.service.SuperAdminServiceV2;

import jakarta.validation.Valid;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v2/admin")
@RequiredArgsConstructor
public class SuperAdminControllerV2 {
    
    private final SuperAdminServiceV2 superAdminServiceV2;
    
    @PostMapping("/super-admin")
    public Mono<ResponseEntity<Map<String, Object>>> createSuperAdmin(@Valid @RequestBody SuperAdminRequest request) {
        log.info("슈퍼 관리자 등록 API 호출: {}", request.getUsername());
        return superAdminServiceV2.createSuperAdmin(request);
    }
    
    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, Object>>> health() {
        return Mono.just(ResponseEntity.ok(Map.of(
            "status", "UP",
            "message", "슈퍼 관리자 API 정상 작동 중"
        )));
    }
} 