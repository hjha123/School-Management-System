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
public class LoginDto {
    @NotBlank(message = "Username Or Email is must")
    private String usernameOrEmail;
    @NotBlank(message = "Password must not be blank")
    private String password;
}
