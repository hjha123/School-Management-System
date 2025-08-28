package edu.zia.international.school.controller;

import edu.zia.international.school.dto.student.CreateStudentRequest;
import edu.zia.international.school.dto.student.StudentResponse;
import edu.zia.international.school.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;
    private static final Logger logger = LoggerFactory.getLogger(StudentController.class);

    // Only Admin can create students
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<StudentResponse> createStudent(@Valid @RequestBody CreateStudentRequest request) {
        logger.info("Received request to create student: {} {}", request.getFirstName(), request.getLastName());
        StudentResponse response = studentService.createStudent(request);
        logger.info("Student created successfully with ID: {}", response.getStudentId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Admin can view all students, students can view their own info
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<StudentResponse>> getAllStudents() {
        logger.info("Fetching all students");
        List<StudentResponse> students = studentService.getAllStudents();
        logger.info("Total students fetched: {}", students.size());
        return ResponseEntity.ok(students);
    }

    // Admin can view any student, student can view only their own
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @GetMapping("/{studentId}")
    public ResponseEntity<StudentResponse> getStudentById(@PathVariable String studentId) {
        logger.info("Fetching student with ID: {}", studentId);
        StudentResponse student = studentService.getStudentById(studentId);
        logger.info("Student fetched successfully: {} {}", student.getFirstName(), student.getLastName());
        return ResponseEntity.ok(student);
    }

    // Only Admin can update students
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{studentId}")
    public ResponseEntity<StudentResponse> updateStudent(
            @PathVariable String studentId,
            @RequestBody CreateStudentRequest request
    ) {
        logger.info("Updating student with ID: {}", studentId);
        StudentResponse updatedStudent = studentService.updateStudent(studentId, request);
        logger.info("Student updated successfully: {} {}", updatedStudent.getFirstName(), updatedStudent.getLastName());
        return ResponseEntity.ok(updatedStudent);
    }

    // Only Admin can delete students
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{studentId}")
    public ResponseEntity<Void> deleteStudent(@PathVariable String studentId) {
        logger.info("Deleting student with ID: {}", studentId);
        studentService.deleteStudent(studentId);
        logger.info("Student deleted successfully with ID: {}", studentId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{studentId}/upload-profile-image")
    public ResponseEntity<StudentResponse> uploadProfileImage(
            @PathVariable String studentId,
            @RequestParam("image") MultipartFile imageFile) {
        logger.info("Received request to upload profile image for studentId: {}", studentId);
        StudentResponse updated = studentService.uploadProfileImage(studentId, imageFile);
        logger.info("Profile image uploaded successfully for studentId: {}", studentId);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentResponse> getMyProfile(Authentication authentication) {
        String username = authentication.getName();
        logger.info("Fetching profile for logged-in student: {}", username);

        StudentResponse studentProfile = studentService.getStudentByUsername(username);
        return ResponseEntity.ok(studentProfile);
    }

    // 🔹 Fetch all students by grade & section (for assignments submissions)
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    @GetMapping("/by-grade-section")
    public ResponseEntity<List<StudentResponse>> getStudentsByGradeAndSection(
            @RequestParam String gradeName,
            @RequestParam(required = false) String sectionName) {
        logger.info("Fetching students for Grade '{}' and Section '{}'", gradeName, sectionName);

        try {
            List<StudentResponse> students = studentService.getStudentsByGradeAndSection(gradeName, sectionName);
            logger.info("Total students fetched: {}", students.size());
            return ResponseEntity.ok(students);
        } catch (Exception ex) {
            logger.error("Error fetching students for Grade '{}' Section '{}': {}", gradeName, sectionName, ex.getMessage(), ex);
            return ResponseEntity.status(500).body(null);
        }
    }

    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    @GetMapping("/search")
    public ResponseEntity<List<StudentResponse>> searchStudents(
            @RequestParam(required = false) Long gradeId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) String gradeName,
            @RequestParam(required = false) String sectionName,
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) String name) {

        logger.info("Searching students with filters - gradeId: {}, sectionId: {}, gradeName: {}, sectionName: {}, studentId: {}, name: {}",
                gradeId, sectionId, gradeName, sectionName, studentId, name);

        // If no filters provided
        if (gradeId == null && sectionId == null &&
                gradeName == null && sectionName == null &&
                studentId == null && name == null) {
            logger.warn("Search request received without any filters");
            throw new IllegalArgumentException("At least one filter must be provided (gradeId/sectionId/gradeName/sectionName/studentId/name)");
        }

        List<StudentResponse> students = studentService.searchStudents(gradeId, sectionId, gradeName, sectionName, studentId, name);
        return ResponseEntity.ok(students);
    }

}
