package edu.zia.international.school.dto.teacher;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class UpdateTeacherRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
    private String phone;

    @NotEmpty(message = "At least one subject ID is required")
    private List<@NotNull(message = "Subject ID cannot be null") Long> subjectIds;

    @NotBlank(message = "Gender is required")
    private String gender;

    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Qualification is required")
    private String qualification;

    @NotBlank(message = "Address is required")
    private String address;

    @NotNull(message = "Joining date is required")
    private LocalDate joiningDate;

    @Min(value = 0, message = "Experience must be a positive number")
    private Integer experienceYears;
}
