package smart.ai.admin.v2.admin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smart.ai.admin.v2.auth.dto.UserDto;
import smart.ai.admin.v2.auth.dto.RoleDto;
import smart.ai.admin.v2.auth.dto.MenuDto;
import smart.ai.admin.v2.admin.service.AdminServiceV2;

import java.util.List;

/**
 * 관리자 컨트롤러 V2 - 관리자 기능 API 엔드포인트
 * 
 * 이 컨트롤러는 관리자 권한을 가진 사용자만 접근할 수 있는 관리 기능을 제공합니다.
 * 사용자, 역할, 메뉴 관리 및 대시보드 통계 기능을 포함합니다.
 * 
 * 주요 기능:
 * - 사용자 관리 (CRUD)
 * - 역할 관리 (CRUD)
 * - 메뉴 관리 (CRUD)
 * - 대시보드 통계 조회
 * 
 * 접근 권한: SUPER_ADMIN, ADMIN
 * 
 * @author Yongho Kim
 * @version 2.0
 * @since 2025
 */
@RestController
@RequestMapping("/api/v2/admin")
@RequiredArgsConstructor
public class AdminControllerV2 {

    private final AdminServiceV2 adminService;

    // ==================== 사용자 관리 API ====================
    
    /**
     * 모든 사용자 목록 조회
     * 
     * @return 사용자 목록
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    /**
     * 특정 사용자 조회
     * 
     * @param id 사용자 ID
     * @return 사용자 정보
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUserById(id));
    }

    /**
     * 새 사용자 생성
     * 
     * @param userDto 사용자 정보
     * @return 생성된 사용자 정보
     */
    @PostMapping("/users")
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto) {
        return ResponseEntity.ok(adminService.createUser(userDto));
    }

    /**
     * 사용자 정보 수정
     * 
     * @param id 사용자 ID
     * @param userDto 수정할 사용자 정보
     * @return 수정된 사용자 정보
     */
    @PutMapping("/users/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @RequestBody UserDto userDto) {
        return ResponseEntity.ok(adminService.updateUser(id, userDto));
    }

    /**
     * 사용자 삭제
     * 
     * @param id 사용자 ID
     * @return 삭제 결과
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    // ==================== 역할 관리 API ====================
    
    /**
     * 모든 역할 목록 조회
     * 
     * @return 역할 목록
     */
    @GetMapping("/roles")
    public ResponseEntity<List<RoleDto>> getAllRoles() {
        return ResponseEntity.ok(adminService.getAllRoles());
    }

    /**
     * 특정 역할 조회
     * 
     * @param id 역할 ID
     * @return 역할 정보
     */
    @GetMapping("/roles/{id}")
    public ResponseEntity<RoleDto> getRoleById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getRoleById(id));
    }

    /**
     * 새 역할 생성
     * 
     * @param roleDto 역할 정보
     * @return 생성된 역할 정보
     */
    @PostMapping("/roles")
    public ResponseEntity<RoleDto> createRole(@RequestBody RoleDto roleDto) {
        return ResponseEntity.ok(adminService.createRole(roleDto));
    }

    /**
     * 역할 정보 수정
     * 
     * @param id 역할 ID
     * @param roleDto 수정할 역할 정보
     * @return 수정된 역할 정보
     */
    @PutMapping("/roles/{id}")
    public ResponseEntity<RoleDto> updateRole(@PathVariable Long id, @RequestBody RoleDto roleDto) {
        return ResponseEntity.ok(adminService.updateRole(id, roleDto));
    }

    /**
     * 역할 삭제
     * 
     * @param id 역할 ID
     * @return 삭제 결과
     */
    @DeleteMapping("/roles/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        adminService.deleteRole(id);
        return ResponseEntity.ok().build();
    }

    // ==================== 메뉴 관리 API ====================
    
    /**
     * 모든 메뉴 목록 조회
     * 
     * @return 메뉴 목록
     */
    @GetMapping("/menus")
    public ResponseEntity<List<MenuDto>> getAllMenus() {
        return ResponseEntity.ok(adminService.getAllMenus());
    }

    /**
     * 특정 메뉴 조회
     * 
     * @param id 메뉴 ID
     * @return 메뉴 정보
     */
    @GetMapping("/menus/{id}")
    public ResponseEntity<MenuDto> getMenuById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getMenuById(id));
    }

    /**
     * 새 메뉴 생성
     * 
     * @param menuDto 메뉴 정보
     * @return 생성된 메뉴 정보
     */
    @PostMapping("/menus")
    public ResponseEntity<MenuDto> createMenu(@RequestBody MenuDto menuDto) {
        return ResponseEntity.ok(adminService.createMenu(menuDto));
    }

    /**
     * 메뉴 정보 수정
     * 
     * @param id 메뉴 ID
     * @param menuDto 수정할 메뉴 정보
     * @return 수정된 메뉴 정보
     */
    @PutMapping("/menus/{id}")
    public ResponseEntity<MenuDto> updateMenu(@PathVariable Long id, @RequestBody MenuDto menuDto) {
        return ResponseEntity.ok(adminService.updateMenu(id, menuDto));
    }

    /**
     * 메뉴 삭제
     * 
     * @param id 메뉴 ID
     * @return 삭제 결과
     */
    @DeleteMapping("/menus/{id}")
    public ResponseEntity<Void> deleteMenu(@PathVariable Long id) {
        adminService.deleteMenu(id);
        return ResponseEntity.ok().build();
    }

    // ==================== 대시보드 API ====================
    
    /**
     * 대시보드 통계 정보 조회
     * 
     * @return 대시보드 통계 데이터
     */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<Object> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }
} 