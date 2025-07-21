-- 데이터베이스 초기화 스크립트
-- 애플리케이션 시작 시 자동 실행됨

-- 기존 데이터 정리 (필요한 경우)
-- DELETE FROM user_roles;
-- DELETE FROM role_menus;

-- 기본 역할 생성 (중복 방지)
INSERT IGNORE INTO roles (name, description, enabled, created_at, updated_at) VALUES
('SUPER_ADMIN', '시스템 최고 관리자', 1, NOW(), NOW()),
('ADMIN', '일반 관리자', 1, NOW(), NOW()),
('USER', '일반 사용자', 1, NOW(), NOW());

-- 기본 메뉴 생성 (중복 방지)
INSERT IGNORE INTO menus (name, description, url, icon, sort_order, enabled, type, created_at, updated_at) VALUES
('대시보드', '관리자 대시보드', '/admin/dashboard', 'bi bi-speedometer2', 1, 1, 'MENU', NOW(), NOW()),
('사용자 관리', '사용자 관리', '/admin/users', 'bi bi-people', 2, 1, 'MENU', NOW(), NOW()),
('역할 관리', '역할 관리', '/admin/roles', 'bi bi-person-badge', 3, 1, 'MENU', NOW(), NOW()),
('메뉴 관리', '메뉴 관리', '/admin/menus', 'bi bi-list', 4, 1, 'MENU', NOW(), NOW());



-- 기존 사용자들의 role_id 업데이트 (기본값으로 USER 역할 할당)
UPDATE users SET role_id = (SELECT id FROM roles WHERE name = 'USER' LIMIT 1) WHERE role_id IS NULL;

-- 슈퍼 관리자 사용자가 있으면 SUPER_ADMIN 역할 할당
UPDATE users u 
JOIN roles r ON r.name = 'SUPER_ADMIN' 
SET u.role_id = r.id 
WHERE u.username = 'superadmin' OR u.username LIKE '%admin%'; 