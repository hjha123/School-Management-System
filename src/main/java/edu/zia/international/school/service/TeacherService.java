package edu.zia.international.school.service;


import edu.zia.international.school.dto.teacher.CreateTeacherRequest;
import edu.zia.international.school.dto.teacher.TeacherResponse;
import edu.zia.international.school.dto.teacher.UpdateTeacherRequest;

import java.util.List;

public interface TeacherService {
    TeacherResponse createTeacher(CreateTeacherRequest request);
    List<TeacherResponse> getAllTeachers();
    TeacherResponse getTeacherById(Long id);
    TeacherResponse updateTeacher(Long id, UpdateTeacherRequest request);
    void deleteTeacher(Long id);

}

