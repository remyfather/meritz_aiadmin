package smart.ai.admin.v2.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smart.ai.admin.v2.auth.dto.UserDto;
import smart.ai.admin.v2.auth.dto.RoleDto;
import smart.ai.admin.v2.auth.dto.MenuDto;
import smart.ai.admin.v2.auth.service.AdminServiceV2;

import java.util.List;

@RestController
@RequestMapping("/api/v2/admin")
@RequiredArgsConstructor
public class AdminControllerV2 {

    private final AdminServiceV2 adminService;

    // 사용자 관리
    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUserById(id));
    }

    @PostMapping("/users")
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto) {
        return ResponseEntity.ok(adminService.createUser(userDto));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @RequestBody UserDto userDto) {
        return ResponseEntity.ok(adminService.updateUser(id, userDto));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    // 역할 관리
    @GetMapping("/roles")
    public ResponseEntity<List<RoleDto>> getAllRoles() {
        return ResponseEntity.ok(adminService.getAllRoles());
    }

    @GetMapping("/roles/{id}")
    public ResponseEntity<RoleDto> getRoleById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getRoleById(id));
    }

    @PostMapping("/roles")
    public ResponseEntity<RoleDto> createRole(@RequestBody RoleDto roleDto) {
        return ResponseEntity.ok(adminService.createRole(roleDto));
    }

    @PutMapping("/roles/{id}")
    public ResponseEntity<RoleDto> updateRole(@PathVariable Long id, @RequestBody RoleDto roleDto) {
        return ResponseEntity.ok(adminService.updateRole(id, roleDto));
    }

    @DeleteMapping("/roles/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        adminService.deleteRole(id);
        return ResponseEntity.ok().build();
    }

    // 메뉴 관리
    @GetMapping("/menus")
    public ResponseEntity<List<MenuDto>> getAllMenus() {
        return ResponseEntity.ok(adminService.getAllMenus());
    }

    @GetMapping("/menus/{id}")
    public ResponseEntity<MenuDto> getMenuById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getMenuById(id));
    }

    @PostMapping("/menus")
    public ResponseEntity<MenuDto> createMenu(@RequestBody MenuDto menuDto) {
        return ResponseEntity.ok(adminService.createMenu(menuDto));
    }

    @PutMapping("/menus/{id}")
    public ResponseEntity<MenuDto> updateMenu(@PathVariable Long id, @RequestBody MenuDto menuDto) {
        return ResponseEntity.ok(adminService.updateMenu(id, menuDto));
    }

    @DeleteMapping("/menus/{id}")
    public ResponseEntity<Void> deleteMenu(@PathVariable Long id) {
        adminService.deleteMenu(id);
        return ResponseEntity.ok().build();
    }

    // 대시보드 통계
    @GetMapping("/dashboard/stats")
    public ResponseEntity<Object> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }
} 