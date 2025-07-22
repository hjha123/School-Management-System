package edu.zia.international.school.dto.teacher;

import edu.zia.international.school.enums.TeacherStatus;
import jakarta.persistence.Column;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class TeacherResponse {
    private Long id;
    private String fullName;
    private String email;
    private String username;
    private String phone;
    private List<String> subjects;
    private String gender;
    private LocalDate dateOfBirth;
    private String qualification;
    private String address;
    private LocalDate joiningDate;
    private Integer experienceYears;
    private String gradeName;
    private String sectionName;
    private String empId;
    private TeacherStatus status;
    private String teacherType;
    private String maritalStatus;
    private String emergencyContactInfo;
    private String bloodGroup;
    private String nationality;
    private String aadharNumber;
    private String profileImageUrl;

    public String getProfileImageUrl() {
        if (this.profileImageUrl == null || this.profileImageUrl.isBlank()) {
            return "default-profile.png";
        }
        return profileImageUrl;
    }


}
