package smart.ai.admin.v2.auth.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuperAdminRequest {
    
    @NotBlank(message = "사용자명은 필수입니다")
    @Size(min = 3, max = 50, message = "사용자명은 3-50자 사이여야 합니다")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "사용자명은 영문, 숫자, 언더스코어만 사용 가능합니다")
    private String username;
    
    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 8, max = 100, message = "비밀번호는 8-100자 사이여야 합니다")
    private String password;
    
    @NotBlank(message = "이름은 필수입니다")
    @Size(max = 100, message = "이름은 100자 이하여야 합니다")
    private String name;
    
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    @Size(max = 100, message = "이메일은 100자 이하여야 합니다")
    private String email;
    
    @NotBlank(message = "사번은 필수입니다")
    @Size(max = 20, message = "사번은 20자 이하여야 합니다")
    private String employeeId;
    
    @Pattern(regexp = "^[0-9]{8}$", message = "생년월일은 YYYYMMDD 형식이어야 합니다")
    private String birthDate;
    
    @Size(max = 100, message = "소속팀은 100자 이하여야 합니다")
    private String department;
    
    @Pattern(regexp = "^01[0-9]-[0-9]{4}-[0-9]{4}$", message = "올바른 전화번호 형식이 아닙니다 (예: 010-1234-5678)")
    @Size(max = 20, message = "전화번호는 20자 이하여야 합니다")
    private String phone;
    
    @NotBlank(message = "시스템 키는 필수입니다")
    private String systemKey;
} 