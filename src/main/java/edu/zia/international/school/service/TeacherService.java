package edu.zia.international.school.service;


import edu.zia.international.school.dto.CreateTeacherRequest;
import edu.zia.international.school.dto.TeacherResponse;
import edu.zia.international.school.dto.UpdateTeacherRequest;

import java.util.List;

public interface TeacherService {
    TeacherResponse createTeacher(CreateTeacherRequest request);
    List<TeacherResponse> getAllTeachers();
    TeacherResponse getTeacherById(Long id);
    TeacherResponse updateTeacher(Long id, UpdateTeacherRequest request);
    void deleteTeacher(Long id);

}

