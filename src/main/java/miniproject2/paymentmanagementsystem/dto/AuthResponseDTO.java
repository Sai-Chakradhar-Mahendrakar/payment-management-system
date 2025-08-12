package miniproject2.paymentmanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import miniproject2.paymentmanagementsystem.enums.Role;

@Data
@AllArgsConstructor
public class AuthResponseDTO {
    private String token;
    private String tokenType = "Bearer";
    private Long userId;
    private String name;
    private String email;
    private Role role;

    public AuthResponseDTO(String token, Long userId, String name, String email, Role role) {
        this.token = token;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
    }
}
