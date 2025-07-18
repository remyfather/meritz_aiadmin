package smart.ai.admin.v2.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import smart.ai.admin.v2.common.domain.Role;
import smart.ai.admin.v2.common.domain.User;
import smart.ai.admin.v2.auth.dto.SignupRequest;
import smart.ai.admin.v2.auth.dto.SignupResponse;
import smart.ai.admin.v2.common.repository.RoleRepository;
import smart.ai.admin.v2.common.repository.UserRepository;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignupServiceV2 {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    
    @Transactional
    public Mono<ResponseEntity<Map<String, Object>>> signup(SignupRequest request) {
        return Mono.fromCallable(() -> {
            log.info("=== 회원가입 트랜잭션 시작 ===");
            log.info("회원가입 요청: 사용자명={}, 이메일={}", request.getUsername(), request.getEmail());
            
            // 1. 중복 검사
            if (userRepository.existsByUsername(request.getUsername())) {
                log.warn("회원가입 실패: 사용자명 중복 - {}", request.getUsername());
                log.info("=== 회원가입 트랜잭션 실패 (사용자명 중복) ===");
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "이미 사용 중인 사용자명입니다"
                ));
            }
            
            if (userRepository.existsByEmail(request.getEmail())) {
                log.warn("회원가입 실패: 이메일 중복 - {}", request.getEmail());
                log.info("=== 회원가입 트랜잭션 실패 (이메일 중복) ===");
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "이미 사용 중인 이메일입니다"
                ));
            }
            
            // 2. 기본 역할 조회 (USER 역할)
            Role defaultRole = roleRepository.findByName("USER")
                    .orElseGet(() -> {
                        // 기본 역할이 없으면 USER 역할 생성
                        log.info("USER 역할이 없어 새로 생성합니다.");
                        Role userRole = Role.builder()
                                .name("USER")
                                .description("일반 사용자")
                                .enabled(true)
                                .build();
                        return roleRepository.save(userRole);
                    });
            
            // 3. 사용자 생성
            User user = User.builder()
                    .username(request.getUsername())
                    .name(request.getName())
                    .email(request.getEmail())
                    .phone(request.getPhone())
                    .enabled(true)
                    .role(defaultRole)
                    .build();
            
            // 비밀번호 암호화
            user.setPassword(request.getPassword());
            
            // 4. 사용자 저장
            User savedUser = userRepository.save(user);
            
            log.info("회원가입 성공: 사용자명={}, 역할={}", savedUser.getUsername(), savedUser.getRole().getName());
            log.info("=== 회원가입 트랜잭션 성공 ===");
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "회원가입이 완료되었습니다",
                "user", Map.of(
                    "id", savedUser.getId(),
                    "username", savedUser.getUsername(),
                    "name", savedUser.getName(),
                    "email", savedUser.getEmail(),
                    "role", savedUser.getRole().getName()
                )
            ));
        });
    }
} 