package edu.zia.international.school.dto.student;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateStudentRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
    private String phone;

    @NotBlank(message = "Gender is required")
    private String gender;

    @NotBlank(message = "Date of birth is required")
    private String dateOfBirth;

    @NotBlank(message = "Grade is required")
    private String gradeName;

    private String sectionName;

    private String address;

    private String emergencyContactName;

    private String emergencyContactPhone;

    private String bloodGroup;

    private String nationality;

    private String profileImageUrl;

    @NotBlank(message = "Guardian name is required")
    private String guardianName;

    @NotBlank(message = "Guardian phone is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Guardian phone must be 10 digits")
    private String guardianPhone;

    @NotNull(message = "Admission date is required")
    private LocalDate admissionDate;
}
