# AI Admin System V2

AI 서비스 관리자를 위한 웹 기반 관리 시스템입니다. JWT 기반 인증, 역할 기반 접근 제어(RBAC), 사용자 관리, 시스템 모니터링 기능을 제공합니다.

## 🚀 주요 기능

### 1. 인증 및 권한 관리

- **JWT 기반 인증**: 안전한 토큰 기반 인증 시스템
- **역할 기반 접근 제어(RBAC)**: SUPER_ADMIN, ADMIN, USER 역할 지원
- **자동 토큰 갱신**: refreshToken을 통한 자동 인증 갱신
- **세션 관리**: 안전한 로그인/로그아웃 처리

### 2. 사용자 관리

- **사용자 CRUD**: 사용자 생성, 조회, 수정, 삭제
- **역할 관리**: 역할 생성, 수정, 삭제
- **메뉴 관리**: 시스템 메뉴 구조 관리
- **비밀번호 암호화**: BCrypt를 사용한 안전한 비밀번호 저장

### 3. 시스템 모니터링

- **실시간 모니터링**: AI 워커 상태 실시간 추적
- **통계 대시보드**: 일별/기간별 사용 통계
- **GPU 모니터링**: GPU 사용량 및 성능 모니터링
- **상세 분석**: 시스템 성능 상세 분석

## 🛠 기술 스택

### Backend

- **Framework**: Spring Boot 3.4.3
- **Language**: Java 17
- **Database**: MySQL 8.0 / Microsoft SQL Server
- **ORM**: Spring Data JPA / Hibernate
- **Security**: JWT, BCrypt
- **Build Tool**: Maven

### Frontend

- **Framework**: Bootstrap 5.3.6
- **UI Library**: SweetAlert2 11.21.2
- **Charts**: Highcharts 12.2.0
- **Template Engine**: Thymeleaf
- **JavaScript**: ES6+ (async/await)

## 📁 프로젝트 구조

```
aiadmin/
├── src/main/java/smart/ai/admin/
│   ├── AdminApplication.java          # 메인 애플리케이션 클래스
│   ├── config/                        # 설정 클래스들
│   │   ├── DatabaseConfig.java        # 데이터베이스 설정
│   │   ├── GlobalExceptionHandler.java # 전역 예외 처리
│   │   └── JwtFilter.java             # JWT 필터
│   ├── v2/                           # V2 버전 구현
│   │   ├── auth/                     # 인증 관련
│   │   │   ├── controller/           # 인증 컨트롤러
│   │   │   ├── service/              # 인증 서비스
│   │   │   └── dto/                  # 인증 DTO
│   │   ├── admin/                    # 관리자 기능
│   │   │   ├── controller/           # 관리자 컨트롤러
│   │   │   ├── service/              # 관리자 서비스
│   │   │   └── dto/                  # 관리자 DTO
│   │   ├── monitor/                  # 모니터링 기능
│   │   │   ├── controller/           # 모니터링 컨트롤러
│   │   │   ├── service/              # 모니터링 서비스
│   │   │   └── dto/                  # 모니터링 DTO
│   │   └── common/                   # 공통 기능
│   │       ├── domain/               # 도메인 엔티티
│   │       ├── repository/           # 데이터 접근 계층
│   │       ├── util/                 # 유틸리티
│   │       └── config/               # 공통 설정
│   ├── domain/                       # 기존 도메인 (모니터링용)
│   ├── repository/                   # 기존 리포지토리
│   ├── service/                      # 기존 서비스
│   └── util/                         # 유틸리티 클래스
├── src/main/resources/
│   ├── application.properties        # 메인 설정 파일
│   ├── application-dev.properties    # 개발 환경 설정
│   ├── application-prod.properties   # 운영 환경 설정
│   ├── db/                          # 데이터베이스 스키마
│   └── logback-spring.xml           # 로깅 설정
├── webapp/                          # 프론트엔드 파일들
│   ├── html/                        # HTML 템플릿
│   │   ├── auth/                    # 인증 관련 페이지
│   │   ├── admin/                   # 관리자 페이지
│   │   ├── monitor/                 # 모니터링 페이지
│   │   └── common/                  # 공통 컴포넌트
│   └── static/                      # 정적 리소스
│       ├── css/                     # 스타일시트
│       ├── js/                      # JavaScript 파일
│       └── lib/                     # 외부 라이브러리
└── target/                          # 빌드 결과물
```

## 🚀 실행 방법

### 1. 환경 요구사항

- Java 17 이상
- Maven 3.6 이상
- MySQL 8.0 또는 Microsoft SQL Server

### 2. 데이터베이스 설정

#### MySQL 설정

```properties
# application-dev.properties
spring.datasource.url=jdbc:mysql://localhost:3306/aiadmin?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

#### SQL Server 설정

```properties
# application-dev.properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=aiadmin;encrypt=true;trustServerCertificate=true
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.SQLServerDialect
```

### 3. 애플리케이션 실행

```bash
# 프로젝트 클론
git clone <repository-url>
cd aiadmin

# 의존성 설치 및 빌드
mvn clean install

# 개발 환경으로 실행
mvn spring-boot:run -Dspring.profiles.active=dev
```

### 4. 접속

브라우저에서 `http://localhost:8080`으로 접속

## 🔐 API 엔드포인트

### 인증 API (V2)

- `POST /api/v2/auth/login` - 사용자 로그인
- `POST /api/v2/auth/logout` - 사용자 로그아웃
- `POST /api/v2/auth/refresh` - JWT 토큰 갱신
- `GET /api/v2/auth/me` - 현재 사용자 정보

### 관리자 API (V2)

- `GET /api/v2/admin/users` - 사용자 목록 조회
- `POST /api/v2/admin/users` - 사용자 생성
- `PUT /api/v2/admin/users/{id}` - 사용자 수정
- `DELETE /api/v2/admin/users/{id}` - 사용자 삭제
- `GET /api/v2/admin/roles` - 역할 목록 조회
- `GET /api/v2/admin/menus` - 메뉴 목록 조회
- `GET /api/v2/admin/dashboard/stats` - 대시보드 통계

### 페이지 라우팅

- `/` - 홈페이지
- `/login` - 로그인 페이지
- `/admin/users` - 사용자 관리 페이지

## 👥 사용자 역할

### SUPER_ADMIN

- 모든 기능 접근 가능
- 시스템 전체 관리 권한
- 사용자, 역할, 메뉴 관리

### ADMIN

- 제한된 관리 기능 접근
- 사용자 관리 (자신 제외)
- 모니터링 데이터 조회

### USER

- 기본 사용자 권한
- 개인 정보 조회/수정
- 모니터링 데이터 조회

## 🔧 개발 가이드

### 코드 스타일

- **Java**: Google Java Style Guide 준수
- **JavaScript**: ES6+ 표준 사용
- **HTML/CSS**: Bootstrap 5 가이드라인 준수

### 주석 작성

- 모든 클래스와 메서드에 JavaDoc 주석 작성
- 복잡한 로직에 인라인 주석 추가
- API 엔드포인트에 상세한 설명 추가

### 로깅

- 개발 환경: DEBUG 레벨 로깅
- 운영 환경: INFO 레벨 로깅
- 중요 작업: 상세한 로그 기록

### 보안 고려사항

- JWT 토큰 만료 시간 설정
- 비밀번호 정책 강화
- SQL 인젝션 방지
- XSS 공격 방지

## 🚀 배포

### JAR 파일 생성

```bash
mvn clean package -DskipTests
```

### 운영 환경 실행

```bash
java -jar -Dspring.profiles.active=prod target/aiadmin-1.0.0.jar
```

### Docker 배포 (선택사항)

```dockerfile
FROM openjdk:17-jre-slim
COPY target/aiadmin-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 📝 변경 이력

### V2.0 (2024)

- JWT 기반 인증 시스템 도입
- 역할 기반 접근 제어(RBAC) 구현
- 사용자 관리 시스템 구축
- 프론트엔드 UI/UX 개선
- API 버전 관리 도입

### V1.0 (2024)

- 기본 모니터링 시스템
- MySQL/SQL Server 지원
- 기본 인증 시스템

## 🤝 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 라이선스

이 프로젝트는 Meritz에서 개발한 내부 시스템입니다.

## 📞 문의

개발 관련 문의사항이 있으시면 개발팀에 연락해주세요.

---

**AI Admin Team**  
Version: 2.0  
Last Updated: 2024
