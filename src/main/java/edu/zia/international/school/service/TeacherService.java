package edu.zia.international.school.service;


import edu.zia.international.school.dto.CreateTeacherRequest;
import edu.zia.international.school.dto.TeacherResponse;

public interface TeacherService {
    TeacherResponse createTeacher(CreateTeacherRequest request);
}

