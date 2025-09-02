package edu.zia.international.school.controller;

import edu.zia.international.school.dto.assignment.*;
import edu.zia.international.school.service.AssignmentService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;
    private static final Logger logger = LoggerFactory.getLogger(AssignmentController.class);

    // Only Teacher can create assignments
    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<AssignmentResponse> createAssignment(
            @RequestPart("request") CreateAssignmentRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            Authentication authentication) {

        String teacherId = authentication.getName();
        logger.info("Teacher {} creating assignment '{}'", teacherId, request.getTitle());

        AssignmentResponse response = assignmentService.createAssignmentAsTeacher(request, files, teacherId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    @PutMapping(value = "/{id}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<AssignmentResponse> updateAssignment(
            @PathVariable Long id,
            @RequestPart("request") UpdateAssignmentRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            Authentication authentication) {

        String teacherId = authentication.getName();
        logger.info("Teacher {} updating assignment with ID {}", teacherId, id);

        AssignmentResponse response = assignmentService.updateAssignment(id, request, files, teacherId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssignmentResponse> getAssignmentById(@PathVariable Long id) {
        logger.info("Received request to get assignment by id={}", id);
        AssignmentResponse response = assignmentService.getAssignmentById(id);
        logger.info("Returning assignment id={} title={}", response.getId(), response.getTitle());
        return ResponseEntity.ok(response);
    }


    // Only Teacher can view their own assignments
    @PreAuthorize("hasRole('TEACHER')")
    @GetMapping("/teacher")
    public ResponseEntity<List<AssignmentResponse>> getTeacherAssignments(Authentication authentication) {
        String teacherId = authentication.getName();
        logger.info("Fetching assignments for teacher {}", teacherId);
        return ResponseEntity.ok(assignmentService.getAssignmentsForTeacher(teacherId));
    }

    // Students can view assignments for their grade and section
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/student")
    public ResponseEntity<List<AssignmentResponse>> getStudentAssignments(
            @RequestParam long gradeId,
            @RequestParam long sectionId) {
        logger.info("Fetching assignments for Grade {} Section {}", gradeId, sectionId);
        return ResponseEntity.ok(assignmentService.getAssignmentsForStudent(gradeId, sectionId));
    }

    // Teacher/Admin can view all submissions for an assignment
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    @GetMapping("/{assignmentId}/submissions")
    public ResponseEntity<List<AssignmentSubmissionResponse>> getSubmissions(@PathVariable Long assignmentId) {
        logger.info("Fetching submissions for assignment {}", assignmentId);
        return ResponseEntity.ok(assignmentService.getSubmissions(assignmentId));
    }

    // Only Teacher can update student submission status
    @PreAuthorize("hasRole('TEACHER')")
    @PutMapping("/{assignmentId}/submissions/{studentId}")
    public ResponseEntity<AssignmentSubmissionResponse> updateSubmissionStatus(
            @PathVariable Long assignmentId,
            @PathVariable String studentId,
            @RequestBody SubmissionUpdateRequest request) {
        logger.info("Updating submission for assignment {} student {}", assignmentId, studentId);
        return ResponseEntity.ok(assignmentService.updateSubmissionStatus(assignmentId, studentId, request.getSubmissionStatus(),
                request.getMarks(), request.getFeedback()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAssignment(@PathVariable Long id) {
        logger.info("Request received to delete assignment with id {}", id);
        assignmentService.deleteAssignment(id);
        return ResponseEntity.ok("Assignment deleted successfully.");
    }

    @PreAuthorize("hasRole('TEACHER')")
    @PutMapping("/{id}/close")
    public ResponseEntity<AssignmentResponse> closeAssignment(
            @PathVariable Long id,
            Authentication authentication) {

        String teacherId = authentication.getName();
        AssignmentResponse response = assignmentService.closeAssignment(id, teacherId);
        return ResponseEntity.ok(response);
    }

    // Only Admin can view all assignments
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/all")
    public ResponseEntity<List<AssignmentResponse>> getAllAssignmentsAdmin() {
        logger.info("Admin requested all assignments");
        List<AssignmentResponse> assignments = assignmentService.getAllAssignmentsAdmin();
        return ResponseEntity.ok(assignments);
    }

    // Only Admin can update adminRemarks
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/admin-remarks")
    public ResponseEntity<AssignmentResponse> updateAdminRemarks(
            @PathVariable Long id,
            @RequestBody UpdateAdminRemarkRequest request) {

        logger.info("Admin updating remarks for assignment id={}", id);
        AssignmentResponse response = assignmentService.updateAdminRemarks(id, request.getAdminRemarks());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/admin", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<AssignmentResponse> createAssignmentAsAdmin(
            @RequestPart("request") CreateAssignmentRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            Authentication authentication) {

        String adminId = authentication.getName();
        logger.info("Admin {} creating assignment '{}'", adminId, request.getTitle());

        AssignmentResponse response = assignmentService.createAssignmentAsAdmin(request, files, adminId);
        return ResponseEntity.ok(response);
    }

}
