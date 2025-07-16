package edu.zis.international.school.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDto {
    @NotBlank(message = "Name must not be blank")
    private String name;
    @NotBlank(message = "Username must not be blank")
    private String username;
    @NotBlank(message = "Email must not be blank")
    private String email;
    @NotBlank(message = "Password must not be blank")
    private String password;
    private String role;
}
