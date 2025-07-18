package edu.zia.international.school.dto;

import lombok.Data;

import java.util.List;

@Data
public class TeacherResponse {
    private Long id;
    private String fullName;
    private String email;
    private String username;
    private String phone;
    private List<String> subjects;

}
