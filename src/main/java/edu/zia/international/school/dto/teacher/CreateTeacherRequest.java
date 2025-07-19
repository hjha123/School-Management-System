package edu.zia.international.school.dto.teacher;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreateTeacherRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
    private String phone;

    @NotEmpty(message = "At least one subject must be selected")
    private List<Long> subjectIds;

    @NotBlank(message = "Gender is required")
    @Pattern(regexp = "MALE|FEMALE|OTHER", message = "Gender must be MALE, FEMALE or OTHER")
    private String gender;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Qualification is required")
    private String qualification;

    @Size(max = 255, message = "Address must be 255 characters or less")
    private String address;

    @PastOrPresent(message = "Joining date cannot be in the future")
    private LocalDate joiningDate;

    @Min(value = 0, message = "Experience years cannot be negative")
    private Integer experienceYears;

    // Optional Future-Ready Fields
    private String staffId;
    private String maritalStatus;
    private String emergencyContactInfo;
    private String bloodGroup;
    private String nationality;
    private String aadharNumber;
    private String profileImageUrl;

    @Pattern(regexp = "FULL_TIME|PART_TIME|GUEST|VISITING", message = "Teacher type must be FULL_TIME, PART_TIME, GUEST or VISITING")
    private String teacherType;

    // Optional Grade & Section to be assigned later or at creation
    private Long gradeId;
    private Long sectionId;
}



