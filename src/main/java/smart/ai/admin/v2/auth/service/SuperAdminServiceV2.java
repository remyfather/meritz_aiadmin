package smart.ai.admin.v2.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import smart.ai.admin.v2.common.domain.Role;
import smart.ai.admin.v2.common.domain.User;
import smart.ai.admin.v2.auth.dto.SuperAdminRequest;
import smart.ai.admin.v2.common.repository.RoleRepository;
import smart.ai.admin.v2.common.repository.UserRepository;

import java.util.HashSet;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SuperAdminServiceV2 {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    
    @Value("${app.system.key:AI_ADMIN_2024}")
    private String systemKey;
    
    @Transactional
    public Mono<ResponseEntity<Map<String, Object>>> createSuperAdmin(SuperAdminRequest request) {
        return Mono.fromCallable(() -> {
            log.info("슈퍼 관리자 등록 요청: {}", request.getUsername());
            
            // 1. 시스템 키 검증
            if (!systemKey.equals(request.getSystemKey())) {
                log.warn("슈퍼 관리자 등록 실패: 잘못된 시스템 키");
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "잘못된 시스템 키입니다"
                ));
            }
            
            // 2. 이미 슈퍼 관리자가 있는지 확인
            if (userRepository.existsByUsername(request.getUsername())) {
                log.warn("슈퍼 관리자 등록 실패: 사용자명 중복 - {}", request.getUsername());
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "이미 사용 중인 사용자명입니다"
                ));
            }
            
            if (userRepository.existsByEmail(request.getEmail())) {
                log.warn("슈퍼 관리자 등록 실패: 이메일 중복 - {}", request.getEmail());
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "이미 사용 중인 이메일입니다"
                ));
            }
            
            // 3. SUPER_ADMIN 역할 조회 또는 생성
            Role superAdminRole = roleRepository.findByName("SUPER_ADMIN")
                    .orElseGet(() -> {
                        Role role = Role.builder()
                                .name("SUPER_ADMIN")
                                .description("시스템 최고 관리자")
                                .enabled(true)
                                .build();
                        return roleRepository.save(role);
                    });
            
            // 4. 슈퍼 관리자 사용자 생성
            User superAdmin = User.builder()
                    .username(request.getUsername())
                    .name(request.getName())
                    .email(request.getEmail())
                    .phone(request.getPhone())
                    .enabled(true)
                    .role(superAdminRole)
                    .build();
            
            // 비밀번호 암호화
            superAdmin.setPassword(request.getPassword());
            
            // 5. 사용자 저장
            User savedSuperAdmin = userRepository.save(superAdmin);
            
            log.info("슈퍼 관리자 등록 성공: {}", savedSuperAdmin.getUsername());
            
            return ResponseEntity.ok(Map.of(
                "message", "슈퍼 관리자가 성공적으로 등록되었습니다",
                "username", savedSuperAdmin.getUsername(),
                "name", savedSuperAdmin.getName(),
                "email", savedSuperAdmin.getEmail(),
                "role", "SUPER_ADMIN"
            ));
        });
    }
} 