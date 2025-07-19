package edu.zia.international.school.dto.teacher;

import edu.zia.international.school.enums.TeacherStatus;
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

}
