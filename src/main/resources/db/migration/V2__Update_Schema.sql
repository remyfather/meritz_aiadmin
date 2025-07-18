-- V2 스키마 업데이트 스크립트

-- 1. 기존 테이블 백업 (선택사항)
-- CREATE TABLE users_backup AS SELECT * FROM users;
-- CREATE TABLE roles_backup AS SELECT * FROM roles;
-- CREATE TABLE menus_backup AS SELECT * FROM menus;

-- 2. 기존 데이터 정리
-- 기존 users 테이블에서 status 컬럼을 enabled로 변경
ALTER TABLE users ADD COLUMN enabled BIT DEFAULT 1;

-- 3. roles 테이블 업데이트
-- 기존 status 컬럼을 enabled로 변경
ALTER TABLE roles ADD COLUMN enabled BIT DEFAULT 1;

-- 4. menus 테이블 업데이트
-- 기존 status 컬럼을 enabled로 변경
ALTER TABLE menus ADD COLUMN enabled BIT DEFAULT 1;
-- menu_order를 sort_order로 변경
ALTER TABLE menus ADD COLUMN sort_order INTEGER DEFAULT 0;

-- 5. 기본 역할 생성 (기존 데이터가 없는 경우)
INSERT IGNORE INTO roles (name, description, enabled, created_at, updated_at) VALUES
('SUPER_ADMIN', '시스템 최고 관리자', 1, NOW(), NOW()),
('ADMIN', '일반 관리자', 1, NOW(), NOW()),
('USER', '일반 사용자', 1, NOW(), NOW());

-- 6. users 테이블에 role_id 컬럼 추가
ALTER TABLE users ADD COLUMN role_id BIGINT;

-- 7. 기존 사용자들에게 기본 역할 할당
-- SUPER_ADMIN 역할을 가진 사용자가 있으면 그 사용자에게 SUPER_ADMIN 역할 할당
UPDATE users u 
JOIN roles r ON r.name = 'SUPER_ADMIN' 
SET u.role_id = r.id 
WHERE u.username = 'superadmin' OR u.username LIKE '%admin%';

-- 나머지 사용자들에게 USER 역할 할당
UPDATE users u 
JOIN roles r ON r.name = 'USER' 
SET u.role_id = r.id 
WHERE u.role_id IS NULL;

-- 8. role_id를 NOT NULL로 변경
ALTER TABLE users MODIFY COLUMN role_id BIGINT NOT NULL;

-- 9. 외래키 제약조건 추가
ALTER TABLE users ADD CONSTRAINT FK_users_role_id FOREIGN KEY (role_id) REFERENCES roles(id);

-- 10. 기존 컬럼들 정리 (선택사항 - 데이터 백업 후 실행)
-- ALTER TABLE users DROP COLUMN status;
-- ALTER TABLE roles DROP COLUMN status;
-- ALTER TABLE menus DROP COLUMN status;
-- ALTER TABLE menus DROP COLUMN menu_order; 