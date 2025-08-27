package edu.zia.international.school.controller;

import edu.zia.international.school.dto.student.CreateStudentRequest;
import edu.zia.international.school.dto.student.StudentResponse;
import edu.zia.international.school.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<StudentResponse> createStudent(@RequestBody CreateStudentRequest request) {
        logger.info("Received request to create student: {} {}", request.getFirstName(), request.getLastName());
        StudentResponse response = studentService.createStudent(request);
        logger.info("Student created successfully with ID: {}", response.getStudentId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Admin can view all students, students can view their own info
    @PreAuthorize("hasRole('ADMIN') or hasRole('STUDENT')")
    @GetMapping
    public ResponseEntity<List<StudentResponse>> getAllStudents() {
        logger.info("Fetching all students");
        List<StudentResponse> students = studentService.getAllStudents();
        logger.info("Total students fetched: {}", students.size());
        return ResponseEntity.ok(students);
    }

    // Admin can view any student, student can view only their own
    @PreAuthorize("hasRole('ADMIN') or (hasRole('STUDENT') and #studentId == principal.studentId)")
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
}
