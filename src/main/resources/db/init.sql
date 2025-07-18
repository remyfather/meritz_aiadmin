-- AI Admin V2 Database Initialization Script
-- MySQL 8.0+ Compatible

-- =====================================================
-- 1. 데이터베이스 생성
-- =====================================================
CREATE DATABASE IF NOT EXISTS aiadmin CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE aiadmin;

-- =====================================================
-- 2. 역할(Role) 테이블 초기화
-- =====================================================
INSERT INTO roles (name, description, status, created_at, updated_at) VALUES
('SUPER_ADMIN', '시스템 최고 관리자', 'ACTIVE', NOW(), NOW()),
('ADMIN', '일반 관리자', 'ACTIVE', NOW(), NOW()),
('USER', '일반 사용자', 'ACTIVE', NOW(), NOW()),
('MONITOR', '모니터링 전용 사용자', 'ACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE 
    description = VALUES(description),
    updated_at = NOW();

-- =====================================================
-- 3. 메뉴(Menu) 테이블 초기화
-- =====================================================
INSERT INTO menus (name, description, url, icon, menu_order, type, status, created_at, updated_at) VALUES
-- 관리자 메뉴
('관리자 대시보드', '관리자 메인 페이지', '/admin', 'bi-house', 1, 'MENU', 'ACTIVE', NOW(), NOW()),
('사용자 관리', '사용자 목록 및 관리', '/admin/users', 'bi-people', 2, 'MENU', 'ACTIVE', NOW(), NOW()),
('역할 관리', '역할 및 권한 관리', '/admin/roles', 'bi-shield', 3, 'MENU', 'ACTIVE', NOW(), NOW()),
('메뉴 관리', '메뉴 구조 관리', '/admin/menus', 'bi-list', 4, 'MENU', 'ACTIVE', NOW(), NOW()),

-- 모니터링 메뉴
('모니터링 대시보드', '시스템 모니터링', '/monitor', 'bi-graph-up', 5, 'MENU', 'ACTIVE', NOW(), NOW()),
('일일 통계', '일일 사용 통계', '/monitor/daily', 'bi-calendar', 6, 'MENU', 'ACTIVE', NOW(), NOW()),
('GPU 모니터링', 'GPU 사용량 모니터링', '/monitor/gpu', 'bi-cpu', 7, 'MENU', 'ACTIVE', NOW(), NOW()),
('시스템 상태', '시스템 상태 확인', '/monitor/status', 'bi-activity', 8, 'MENU', 'ACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE 
    description = VALUES(description),
    updated_at = NOW();

-- =====================================================
-- 4. 역할-메뉴 매핑
-- =====================================================
-- SUPER_ADMIN: 모든 메뉴 접근 가능
INSERT INTO role_menus (role_id, menu_id)
SELECT r.id, m.id
FROM roles r, menus m
WHERE r.name = 'SUPER_ADMIN'
ON DUPLICATE KEY UPDATE role_id = role_id;

-- ADMIN: 관리자 메뉴 + 모니터링 메뉴
INSERT INTO role_menus (role_id, menu_id)
SELECT r.id, m.id
FROM roles r, menus m
WHERE r.name = 'ADMIN' AND m.menu_order <= 8
ON DUPLICATE KEY UPDATE role_id = role_id;

-- USER: 기본 메뉴만
INSERT INTO role_menus (role_id, menu_id)
SELECT r.id, m.id
FROM roles r, menus m
WHERE r.name = 'USER' AND m.menu_order IN (1, 5)
ON DUPLICATE KEY UPDATE role_id = role_id;

-- MONITOR: 모니터링 메뉴만
INSERT INTO role_menus (role_id, menu_id)
SELECT r.id, m.id
FROM roles r, menus m
WHERE r.name = 'MONITOR' AND m.menu_order >= 5
ON DUPLICATE KEY UPDATE role_id = role_id;

-- =====================================================
-- 5. 기본 관리자 사용자 생성
-- =====================================================
-- 비밀번호: admin123 (BCrypt 해시)
INSERT INTO users (username, password, name, email, phone, status, created_at, updated_at) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '시스템 관리자', 'admin@meritz.com', '010-1234-5678', 'ACTIVE', NOW(), NOW()),
('user', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '일반 사용자', 'user@meritz.com', '010-2345-6789', 'ACTIVE', NOW(), NOW()),
('monitor', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '모니터링 사용자', 'monitor@meritz.com', '010-3456-7890', 'ACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE 
    name = VALUES(name),
    email = VALUES(email),
    phone = VALUES(phone),
    updated_at = NOW();

-- =====================================================
-- 6. 사용자-역할 매핑
-- =====================================================
-- admin -> SUPER_ADMIN, ADMIN
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'admin' AND r.name IN ('SUPER_ADMIN', 'ADMIN')
ON DUPLICATE KEY UPDATE user_id = user_id;

-- user -> USER
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'user' AND r.name = 'USER'
ON DUPLICATE KEY UPDATE user_id = user_id;

-- monitor -> MONITOR
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'monitor' AND r.name = 'MONITOR'
ON DUPLICATE KEY UPDATE user_id = user_id;

-- =====================================================
-- 7. 결과 확인
-- =====================================================
SELECT '=== 초기화 완료 ===' as status;

SELECT 'Users:' as info;
SELECT id, username, name, email, status FROM users;

SELECT 'Roles:' as info;
SELECT id, name, description, status FROM roles;

SELECT 'Menus:' as info;
SELECT id, name, description, url, menu_order, type, status FROM menus ORDER BY menu_order;

SELECT 'User Roles:' as info;
SELECT u.username, r.name as role_name
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
ORDER BY u.username, r.name;

SELECT 'Role Menus:' as info;
SELECT r.name as role_name, m.name as menu_name, m.url
FROM roles r
JOIN role_menus rm ON r.id = rm.role_id
JOIN menus m ON rm.menu_id = m.id
ORDER BY r.name, m.menu_order; 