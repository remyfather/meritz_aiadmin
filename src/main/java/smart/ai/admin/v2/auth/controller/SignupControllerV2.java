package smart.ai.admin.v2.auth.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import smart.ai.admin.v2.auth.dto.SignupRequest;
import smart.ai.admin.v2.auth.service.SignupServiceV2;

import jakarta.validation.Valid;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v2/auth")
@RequiredArgsConstructor
public class SignupControllerV2 {
    
    private final SignupServiceV2 signupServiceV2;
    
    @PostMapping("/signup")
    public Mono<ResponseEntity<Map<String, Object>>> signup(@Valid @RequestBody SignupRequest request) {
        log.info("회원가입 API 호출: {}", request.getUsername());
        return signupServiceV2.signup(request);
    }
    
    @GetMapping("/signup/check-username")
    public Mono<ResponseEntity<Map<String, Object>>> checkUsername(@RequestParam String username) {
        log.info("사용자명 중복 확인: {}", username);
        return Mono.just(ResponseEntity.ok(Map.of(
            "available", true,
            "message", "사용 가능한 사용자명입니다"
        )));
    }
    
    @GetMapping("/signup/check-email")
    public Mono<ResponseEntity<Map<String, Object>>> checkEmail(@RequestParam String email) {
        log.info("이메일 중복 확인: {}", email);
        return Mono.just(ResponseEntity.ok(Map.of(
            "available", true,
            "message", "사용 가능한 이메일입니다"
        )));
    }
} 