package smart.ai.admin.v2.auth.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RoleDto {
    private Long id;
    private String name;
    private String description;
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 