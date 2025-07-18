package smart.ai.admin.v2.auth.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MenuDto {
    private Long id;
    private String name;
    private String url;
    private String icon;
    private Integer sortOrder;
    private Long parentId;
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 