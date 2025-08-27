package edu.zia.international.school.dto.student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponse {
    private Long id;
    private String studentId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String gender;
    private String dateOfBirth;
    private String gradeName;
    private String sectionName;
    private String address;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String bloodGroup;
    private String nationality;
    private String profileImageUrl;
    private String username;
    private String status;
    private String guardianName;
    private String guardianPhone;
    private LocalDate admissionDate;
}
