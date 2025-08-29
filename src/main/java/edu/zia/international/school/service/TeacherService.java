package edu.zia.international.school.service;


import edu.zia.international.school.dto.teacher.*;
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
    TeacherResponse getTeacherByUsername(String username);
    TeacherResponse partialUpdateByUsername(String username, PartialUpdateTeacherRequest request);
    CurrentUserResponse getEmpIdAndName(String username);
    List<TeacherResponse> searchTeachers(Long gradeId, String gradeName, Long sectionId, String sectionName, String name, String empId, String teacherType);

}

