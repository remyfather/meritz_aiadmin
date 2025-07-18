package smart.ai.admin.v2.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smart.ai.admin.v2.common.domain.User;
import smart.ai.admin.v2.common.domain.Role;
import smart.ai.admin.v2.common.domain.Menu;
import smart.ai.admin.v2.auth.dto.UserDto;
import smart.ai.admin.v2.auth.dto.RoleDto;
import smart.ai.admin.v2.auth.dto.MenuDto;
import smart.ai.admin.v2.common.repository.UserRepository;
import smart.ai.admin.v2.common.repository.RoleRepository;
import smart.ai.admin.v2.common.repository.MenuRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminServiceV2 {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MenuRepository menuRepository;
    private final PasswordEncoder passwordEncoder;

    // 사용자 관리
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToUserDto)
                .collect(Collectors.toList());
    }

    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + id));
        return convertToUserDto(user);
    }

    public UserDto createUser(UserDto userDto) {
        User user = new User();
        user.setName(userDto.getName());
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        
        // 비밀번호 설정 (제공된 경우 사용, 없으면 기본값)
        String password = userDto.getPassword() != null && !userDto.getPassword().trim().isEmpty() 
            ? userDto.getPassword() 
            : "password123";
        user.setPassword(passwordEncoder.encode(password));
        user.setEnabled(userDto.isEnabled());
        
        // 역할 설정
        Role role = roleRepository.findByName(userDto.getRole())
                .orElseThrow(() -> new RuntimeException("역할을 찾을 수 없습니다: " + userDto.getRole()));
        user.setRole(role);
        
        User savedUser = userRepository.save(user);
        return convertToUserDto(savedUser);
    }

    public UserDto updateUser(Long id, UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + id));
        
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setEnabled(userDto.isEnabled());
        
        // 비밀번호가 제공된 경우 업데이트
        if (userDto.getPassword() != null && !userDto.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }
        
        if (userDto.getRole() != null && !userDto.getRole().equals(user.getRole().getName())) {
            Role role = roleRepository.findByName(userDto.getRole())
                    .orElseThrow(() -> new RuntimeException("역할을 찾을 수 없습니다: " + userDto.getRole()));
            user.setRole(role);
        }
        
        User savedUser = userRepository.save(user);
        return convertToUserDto(savedUser);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // 역할 관리
    public List<RoleDto> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::convertToRoleDto)
                .collect(Collectors.toList());
    }

    public RoleDto getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("역할을 찾을 수 없습니다: " + id));
        return convertToRoleDto(role);
    }

    public RoleDto createRole(RoleDto roleDto) {
        Role role = new Role();
        role.setName(roleDto.getName());
        role.setDescription(roleDto.getDescription());
        role.setEnabled(true);
        
        Role savedRole = roleRepository.save(role);
        return convertToRoleDto(savedRole);
    }

    public RoleDto updateRole(Long id, RoleDto roleDto) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("역할을 찾을 수 없습니다: " + id));
        
        role.setName(roleDto.getName());
        role.setDescription(roleDto.getDescription());
        role.setEnabled(roleDto.isEnabled());
        
        Role savedRole = roleRepository.save(role);
        return convertToRoleDto(savedRole);
    }

    public void deleteRole(Long id) {
        roleRepository.deleteById(id);
    }

    // 메뉴 관리
    public List<MenuDto> getAllMenus() {
        return menuRepository.findAll().stream()
                .map(this::convertToMenuDto)
                .collect(Collectors.toList());
    }

    public MenuDto getMenuById(Long id) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("메뉴를 찾을 수 없습니다: " + id));
        return convertToMenuDto(menu);
    }

    public MenuDto createMenu(MenuDto menuDto) {
        Menu menu = new Menu();
        menu.setName(menuDto.getName());
        menu.setUrl(menuDto.getUrl());
        menu.setIcon(menuDto.getIcon());
        menu.setSortOrder(menuDto.getSortOrder());
        menu.setEnabled(true);
        
        if (menuDto.getParentId() != null) {
            Menu parentMenu = menuRepository.findById(menuDto.getParentId())
                    .orElseThrow(() -> new RuntimeException("부모 메뉴를 찾을 수 없습니다: " + menuDto.getParentId()));
            menu.setParent(parentMenu);
        }
        
        Menu savedMenu = menuRepository.save(menu);
        return convertToMenuDto(savedMenu);
    }

    public MenuDto updateMenu(Long id, MenuDto menuDto) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("메뉴를 찾을 수 없습니다: " + id));
        
        menu.setName(menuDto.getName());
        menu.setUrl(menuDto.getUrl());
        menu.setIcon(menuDto.getIcon());
        menu.setSortOrder(menuDto.getSortOrder());
        menu.setEnabled(menuDto.isEnabled());
        
        if (menuDto.getParentId() != null) {
            Menu parentMenu = menuRepository.findById(menuDto.getParentId())
                    .orElseThrow(() -> new RuntimeException("부모 메뉴를 찾을 수 없습니다: " + menuDto.getParentId()));
            menu.setParent(parentMenu);
        } else {
            menu.setParent(null);
        }
        
        Menu savedMenu = menuRepository.save(menu);
        return convertToMenuDto(savedMenu);
    }

    public void deleteMenu(Long id) {
        menuRepository.deleteById(id);
    }

    // 대시보드 통계
    public Map<String, Object> getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalRoles = roleRepository.count();
        long totalMenus = menuRepository.count();
        long enabledUsers = userRepository.countByEnabledTrue();
        
        return Map.of(
            "totalUsers", totalUsers,
            "totalRoles", totalRoles,
            "totalMenus", totalMenus,
            "enabledUsers", enabledUsers,
            "disabledUsers", totalUsers - enabledUsers
        );
    }

    // 변환 메서드들
    private UserDto convertToUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().getName());
        dto.setEnabled(user.isEnabled());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }

    private RoleDto convertToRoleDto(Role role) {
        RoleDto dto = new RoleDto();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        dto.setEnabled(role.isEnabled());
        dto.setCreatedAt(role.getCreatedAt());
        dto.setUpdatedAt(role.getUpdatedAt());
        return dto;
    }

    private MenuDto convertToMenuDto(Menu menu) {
        MenuDto dto = new MenuDto();
        dto.setId(menu.getId());
        dto.setName(menu.getName());
        dto.setUrl(menu.getUrl());
        dto.setIcon(menu.getIcon());
        dto.setSortOrder(menu.getSortOrder());
        dto.setEnabled(menu.isEnabled());
        dto.setCreatedAt(menu.getCreatedAt());
        dto.setUpdatedAt(menu.getUpdatedAt());
        
        if (menu.getParent() != null) {
            dto.setParentId(menu.getParent().getId());
        }
        
        return dto;
    }
} 