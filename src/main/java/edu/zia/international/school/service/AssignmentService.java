package edu.zia.international.school.service;

import edu.zia.international.school.dto.assignment.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AssignmentService {
    AssignmentResponse createAssignment(CreateAssignmentRequest request, List<MultipartFile> files, String teacherId);
    List<AssignmentResponse> getAssignmentsForTeacher(String teacherId);
    List<AssignmentResponse> getAssignmentsForStudent(long gradeId, long sectionId);
    List<AssignmentSubmissionResponse> getSubmissions(Long assignmentId);
    AssignmentSubmissionResponse updateSubmissionStatus(Long assignmentId, String studentId, Double marks, String feedback);
    AssignmentResponse getAssignmentById(Long id);
    void deleteAssignment(Long id);
    AssignmentResponse updateAssignment(Long id, UpdateAssignmentRequest request, List<MultipartFile> files, String teacherId);
}
