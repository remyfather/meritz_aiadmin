package smart.ai.admin.v2.common.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

/**
 * 사용자 도메인 엔티티
 * 
 * 시스템의 사용자 정보를 관리하는 핵심 엔티티입니다.
 * JPA를 사용하여 데이터베이스와 매핑되며, BCrypt를 사용한 비밀번호 암호화를 지원합니다.
 * 
 * 주요 기능:
 * - 사용자 기본 정보 관리 (이름, 이메일, 전화번호 등)
 * - 비밀번호 암호화 및 검증
 * - 역할 기반 권한 관리
 * - 계정 활성화/비활성화
 * - 로그인 시간 추적
 * - 생성/수정 시간 자동 관리
 * 
 * @author Yongho Kim
 * @version 2.0
 * @since 2025
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    
    /**
     * 사용자 고유 ID (자동 생성)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 사용자명 (고유값, 로그인에 사용)
     */
    @Column(unique = true, nullable = false, length = 50)
    private String username;
    
    /**
     * 암호화된 비밀번호 (BCrypt 사용)
     */
    @Column(nullable = false)
    private String password;
    
    /**
     * 사용자 실명
     */
    @Column(nullable = false, length = 100)
    private String name;
    
    /**
     * 이메일 주소 (고유값)
     */
    @Column(unique = true, nullable = false, length = 100)
    private String email;
    
    /**
     * 전화번호
     */
    @Column(length = 20)
    private String phone;
    
    /**
     * 사번 (Employee ID) - 필수 필드
     */
    @Column(nullable = false, length = 20, unique = true)
    private String employeeId;
    
    /**
     * 생년월일 (YYYYMMDD 형식)
     */
    @Column(length = 8)
    private String birthDate;
    
    /**
     * 소속팀
     */
    @Column(length = 100)
    private String department;
    
    /**
     * 계정 활성화 상태 (true: 활성, false: 비활성)
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;
    
    /**
     * 마지막 로그인 시간
     */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    /**
     * 계정 생성 시간 (자동 설정, 수정 불가)
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 계정 수정 시간 (자동 업데이트)
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * 사용자 역할 (Many-to-One 관계)
     * EAGER 로딩으로 즉시 로드
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
    
    /**
     * 비밀번호 암호화를 위한 BCrypt 인코더
     * (정적 필드이므로 서비스에서 주입받아 사용)
     */
    private static BCryptPasswordEncoder passwordEncoder;
    
    /**
     * 엔티티 생성 시 자동 호출
     * 생성 시간과 수정 시간을 현재 시간으로 설정
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * 엔티티 수정 시 자동 호출
     * 수정 시간을 현재 시간으로 업데이트
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * 비밀번호 설정 (이미 암호화된 비밀번호)
     * 
     * @param encodedPassword 암호화된 비밀번호
     */
    public void setPassword(String encodedPassword) {
        this.password = encodedPassword;
    }
    
    /**
     * 비밀번호 검증
     * 
     * @param rawPassword 검증할 평문 비밀번호
     * @return 비밀번호 일치 여부
     */
    public boolean checkPassword(String rawPassword) {
        if (passwordEncoder == null) {
            passwordEncoder = new BCryptPasswordEncoder();
        }
        return passwordEncoder.matches(rawPassword, this.password);
    }
} 