package edu.zia.international.school.service;


import edu.zia.international.school.dto.student.CreateStudentRequest;
import edu.zia.international.school.dto.student.StudentResponse;

import java.util.List;

public interface StudentService {

    StudentResponse createStudent(CreateStudentRequest request);

    List<StudentResponse> getAllStudents();

    StudentResponse getStudentById(String studentId);

    StudentResponse updateStudent(String studentId, CreateStudentRequest request);

    void deleteStudent(String studentId);
}
