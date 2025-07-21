package smart.ai.admin.v2.auth.dto;

import lombok.Data;
import smart.ai.admin.v2.common.domain.Role;

import java.time.LocalDateTime;

@Data
public class UserDto {
    private Long id;
    private String name;
    private String username;
    private String email;
    private String phone;
    private String employeeId;
    private String birthDate;
    private String department;
    private String password;
    private String role;
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 