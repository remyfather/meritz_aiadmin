package smart.ai.admin.v2.auth.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupResponse {
    
    private Long id;
    private String username;
    private String name;
    private String email;
    private String phone;
    private String status;
    private Set<String> roles;
    private LocalDateTime createdAt;
    private String message;
} 