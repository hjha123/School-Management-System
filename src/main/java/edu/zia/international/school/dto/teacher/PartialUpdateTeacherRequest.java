package edu.zia.international.school.dto.teacher;

import lombok.Data;

@Data
public class PartialUpdateTeacherRequest {
    private String maritalStatus;
    private String emergencyContactInfo;
    private String bloodGroup;
    private String nationality;
    private String aadharNumber;
    private Integer experienceYears;
    private String address;
}
