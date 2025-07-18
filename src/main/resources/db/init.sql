-- AI Admin Database Initialization Script
-- MySQL 8.0+

-- 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS aiadmin CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE aiadmin;

-- 사용자 테이블
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    status ENUM('ACTIVE', 'INACTIVE', 'LOCKED', 'DELETED') NOT NULL DEFAULT 'ACTIVE',
    last_login_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_status (status)
);

-- 롤 테이블
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    status ENUM('ACTIVE', 'INACTIVE', 'DELETED') NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (name),
    INDEX idx_status (status)
);

-- 메뉴 테이블
CREATE TABLE IF NOT EXISTS menus (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(200),
    url VARCHAR(200),
    icon VARCHAR(50),
    menu_order INT DEFAULT 0,
    type ENUM('MENU', 'SUBMENU', 'BUTTON', 'API') NOT NULL DEFAULT 'MENU',
    status ENUM('ACTIVE', 'INACTIVE', 'DELETED') NOT NULL DEFAULT 'ACTIVE',
    parent_id BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES menus(id) ON DELETE SET NULL,
    INDEX idx_parent_id (parent_id),
    INDEX idx_status (status),
    INDEX idx_type (type),
    INDEX idx_order (menu_order)
);

-- 사용자-롤 관계 테이블
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- 롤-메뉴 관계 테이블
CREATE TABLE IF NOT EXISTS role_menus (
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, menu_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (menu_id) REFERENCES menus(id) ON DELETE CASCADE
);

-- 초기 데이터 삽입

-- 기본 롤 생성
INSERT INTO roles (name, description) VALUES
('SUPER_ADMIN', '시스템 최고 관리자'),
('ADMIN', '일반 관리자'),
('USER', '일반 사용자'),
('MONITOR', '모니터링 전용 사용자')
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- 기본 메뉴 생성
INSERT INTO menus (name, description, url, icon, menu_order, type, parent_id) VALUES
-- 메인 메뉴
('대시보드', '메인 대시보드', '/', 'bi-house', 1, 'MENU', NULL),
('모니터링', 'AI 워커 모니터링', '/monitor', 'bi-graph-up', 2, 'MENU', NULL),
('관리자', '시스템 관리', '/admin', 'bi-gear', 3, 'MENU', NULL),
('사용자 관리', '사용자 및 권한 관리', '/admin/users', 'bi-people', 4, 'MENU', NULL),

-- 모니터링 서브메뉴
('일별 모니터링', '일별 통계', '/monitor/daily', 'bi-calendar-day', 1, 'SUBMENU', 
 (SELECT id FROM menus WHERE name = '모니터링' LIMIT 1)),
('기간별 모니터링', '기간별 통계', '/monitor/period', 'bi-calendar-range', 2, 'SUBMENU', 
 (SELECT id FROM menus WHERE name = '모니터링' LIMIT 1)),
('상세 모니터링', '상세 정보', '/monitor/detail', 'bi-list-ul', 3, 'SUBMENU', 
 (SELECT id FROM menus WHERE name = '모니터링' LIMIT 1)),
('GPU 모니터링', 'GPU 사용량', '/monitor/gpu-daily', 'bi-cpu', 4, 'SUBMENU', 
 (SELECT id FROM menus WHERE name = '모니터링' LIMIT 1)),

-- 관리자 서브메뉴
('서비스 관리', 'AI 서비스 관리', '/admin/service', 'bi-server', 1, 'SUBMENU', 
 (SELECT id FROM menus WHERE name = '관리자' LIMIT 1)),
('시스템 설정', '시스템 설정', '/admin/config', 'bi-sliders', 2, 'SUBMENU', 
 (SELECT id FROM menus WHERE name = '관리자' LIMIT 1)),

-- 사용자 관리 서브메뉴
('사용자 목록', '사용자 관리', '/admin/users/list', 'bi-person-lines-fill', 1, 'SUBMENU', 
 (SELECT id FROM menus WHERE name = '사용자 관리' LIMIT 1)),
('롤 관리', '권한 롤 관리', '/admin/users/roles', 'bi-shield-check', 2, 'SUBMENU', 
 (SELECT id FROM menus WHERE name = '사용자 관리' LIMIT 1)),
('메뉴 관리', '메뉴 권한 관리', '/admin/users/menus', 'bi-list-nested', 3, 'SUBMENU', 
 (SELECT id FROM menus WHERE name = '사용자 관리' LIMIT 1))
ON DUPLICATE KEY UPDATE 
    description = VALUES(description),
    url = VALUES(url),
    icon = VALUES(icon),
    menu_order = VALUES(menu_order);

-- 기본 사용자 생성 (비밀번호: admin123!)
INSERT INTO users (username, password, name, email, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '시스템 관리자', 'admin@meritz.com', 'ACTIVE'),
('user', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '일반 사용자', 'user@meritz.com', 'ACTIVE'),
('monitor', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '모니터링 사용자', 'monitor@meritz.com', 'ACTIVE')
ON DUPLICATE KEY UPDATE 
    name = VALUES(name),
    email = VALUES(email);

-- 사용자-롤 관계 설정
INSERT INTO user_roles (user_id, role_id) VALUES
-- admin 사용자는 SUPER_ADMIN 롤
((SELECT id FROM users WHERE username = 'admin'), (SELECT id FROM roles WHERE name = 'SUPER_ADMIN')),
-- user 사용자는 USER 롤
((SELECT id FROM users WHERE username = 'user'), (SELECT id FROM roles WHERE name = 'USER')),
-- monitor 사용자는 MONITOR 롤
((SELECT id FROM users WHERE username = 'monitor'), (SELECT id FROM roles WHERE name = 'MONITOR'))
ON DUPLICATE KEY UPDATE user_id = VALUES(user_id);

-- 롤-메뉴 관계 설정
INSERT INTO role_menus (role_id, menu_id) 
SELECT r.id, m.id 
FROM roles r, menus m 
WHERE r.name = 'SUPER_ADMIN'  -- SUPER_ADMIN은 모든 메뉴 접근 가능
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

-- USER 롤은 모니터링 메뉴만 접근 가능
INSERT INTO role_menus (role_id, menu_id)
SELECT r.id, m.id 
FROM roles r, menus m 
WHERE r.name = 'USER' 
AND m.name IN ('대시보드', '일별 모니터링', '기간별 모니터링', '상세 모니터링', 'GPU 모니터링')
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

-- MONITOR 롤은 모니터링 메뉴만 접근 가능
INSERT INTO role_menus (role_id, menu_id)
SELECT r.id, m.id 
FROM roles r, menus m 
WHERE r.name = 'MONITOR' 
AND m.name IN ('대시보드', '일별 모니터링', '기간별 모니터링', '상세 모니터링', 'GPU 모니터링')
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

-- ADMIN 롤은 관리자 메뉴까지 접근 가능
INSERT INTO role_menus (role_id, menu_id)
SELECT r.id, m.id 
FROM roles r, menus m 
WHERE r.name = 'ADMIN' 
AND m.name NOT IN ('사용자 목록', '롤 관리', '메뉴 관리')
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id); 