package miniproject2.paymentmanagementsystem.dto;

import lombok.Data;
import miniproject2.paymentmanagementsystem.enums.Role;

@Data
public class UserResponseDTO {
    private Long id;
    private String name;
    private String email;
    private Role role;
}
