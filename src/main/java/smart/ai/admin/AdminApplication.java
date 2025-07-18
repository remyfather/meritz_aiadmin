package smart.ai.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AI Admin Application - 메인 애플리케이션 클래스
 * 
 * 이 애플리케이션은 AI 서비스 관리자를 위한 웹 기반 관리 시스템입니다.
 * 주요 기능:
 * - 사용자 인증 및 권한 관리 (JWT 기반)
 * - 역할 기반 접근 제어 (RBAC)
 * - 사용자 관리 (CRUD)
 * - 시스템 모니터링
 * 
 * @author Yongho Kim
 * @version 2.0
 * @since 2025
 */
@SpringBootApplication
public class AdminApplication {

	/**
	 * 애플리케이션 진입점
	 * Spring Boot 애플리케이션을 시작합니다.
	 * 
	 * @param args 명령행 인수
	 */
	public static void main(String[] args) {
		SpringApplication.run(AdminApplication.class, args);
	}

}
