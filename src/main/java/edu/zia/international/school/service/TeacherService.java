package edu.zia.international.school.service;


import edu.zia.international.school.dto.teacher.CreateTeacherRequest;
import edu.zia.international.school.dto.teacher.TeacherResponse;
import edu.zia.international.school.dto.teacher.UpdateTeacherRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TeacherService {
    TeacherResponse createTeacher(CreateTeacherRequest request);
    List<TeacherResponse> getAllTeachers();
    TeacherResponse getTeacherById(Long id);
    TeacherResponse getByEmpId(String empId);
    TeacherResponse updateTeacher(Long id, UpdateTeacherRequest request);
    void deleteTeacher(Long id);
    void deleteTeacherByEmpId(String empId);
    TeacherResponse updateTeacherByEmpId(String empId, UpdateTeacherRequest request);
    TeacherResponse uploadProfileImage(String empId, MultipartFile imageFile);

}

