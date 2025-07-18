# Meritz AI Admin

AI 관리자 시스템으로, AI 워커 모니터링 및 관리 기능을 제공합니다.

## 기술 스택

- **Backend**: Spring Boot 3.4.3, Java 17
- **Frontend**: HTML, CSS, JavaScript, Bootstrap 5.3.6
- **Database**: Microsoft SQL Server
- **Build Tool**: Maven
- **Charts**: Highcharts 12.2.0

## 주요 기능

### 1. 인증 시스템

- JWT 기반 인증
- 관리자 로그인/로그아웃
- 세션 관리

### 2. 모니터링 시스템

- 실시간 AI 워커 상태 모니터링
- 일별/기간별 통계 데이터
- GPU 사용량 모니터링
- 상세 모니터링 정보

### 3. 관리자 기능

- 서비스 관리
- 시스템 설정
- 사용자 관리

## 프로젝트 구조

```
aiadmin/
├── src/main/java/smart/ai/admin/
│   ├── config/          # 설정 클래스들
│   ├── controller/      # REST API 컨트롤러
│   ├── domain/          # 도메인 모델
│   ├── repository/      # 데이터 접근 계층
│   ├── service/         # 비즈니스 로직
│   ├── util/            # 유틸리티 클래스
│   └── view/            # 뷰 컨트롤러
├── src/main/resources/  # 설정 파일들
├── webapp/              # 프론트엔드 파일들
│   ├── html/            # HTML 템플릿
│   └── static/          # 정적 리소스 (CSS, JS, 이미지)
└── target/              # 빌드 결과물
```

## 실행 방법

### 1. 환경 설정

- Java 17 이상 설치
- Maven 설치
- Microsoft SQL Server 설정

### 2. 데이터베이스 설정

`src/main/resources/application.properties` 파일에서 데이터베이스 연결 정보를 설정하세요.

### 3. 애플리케이션 실행

```bash
# Maven으로 빌드
mvn clean install

# 애플리케이션 실행
mvn spring-boot:run
```

### 4. 접속

브라우저에서 `http://localhost:8080`으로 접속하세요.

## API 엔드포인트

### 인증

- `POST /auth/login` - 로그인
- `POST /auth/logout` - 로그아웃

### 모니터링

- `GET /monitor/daily` - 일별 모니터링 데이터
- `GET /monitor/period` - 기간별 모니터링 데이터
- `GET /monitor/detail` - 상세 모니터링 정보
- `GET /monitor/gpu-daily` - GPU 일별 데이터

### 관리자

- `GET /admin/service` - 서비스 관리 페이지
- `POST /admin/command` - 명령어 실행

## 개발 환경 설정

### IDE 설정

- IntelliJ IDEA 또는 Eclipse 사용 권장
- Spring Boot 플러그인 설치
- Lombok 플러그인 설치 (필요시)

### 코드 스타일

- Java 코드는 Google Java Style Guide 준수
- HTML/CSS/JavaScript는 Bootstrap 5 가이드라인 준수

## 배포

### JAR 파일 생성

```bash
mvn clean package
```

### 실행

```bash
java -jar target/aiadmin-1.0.0.jar
```

## 라이선스

이 프로젝트는 Meritz에서 개발한 내부 시스템입니다.

## 문의

개발 관련 문의사항이 있으시면 개발팀에 연락해주세요.
