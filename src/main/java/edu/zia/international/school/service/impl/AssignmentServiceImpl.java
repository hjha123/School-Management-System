package edu.zia.international.school.service.impl;

import edu.zia.international.school.dto.assignment.*;
import edu.zia.international.school.entity.*;
import edu.zia.international.school.enums.AssignmentStatus;
import edu.zia.international.school.enums.SubmissionStatus;
import edu.zia.international.school.exception.ResourceNotFoundException;
import edu.zia.international.school.repository.*;
import edu.zia.international.school.service.AssignmentService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository assignmentSubmissionRepository;
    private final AssignmentSubmissionRepository submissionRepository;
    private final GradeRepository gradeRepository;
    private final SectionRepository sectionRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(AssignmentServiceImpl.class);

    @Override
    @Transactional
    public AssignmentResponse createAssignmentAsTeacher(CreateAssignmentRequest request, List<MultipartFile> files, String userId) {

        // 🔹 Resolve Grade
        Grade grade = null;
        String gradeName;
        if (request.getGradeId() > 0) {
            grade = gradeRepository.findById(request.getGradeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Grade not found with id: " + request.getGradeId()));
            gradeName = grade.getName();
        } else {
            gradeName = null;
        }

        // 🔹 Resolve Section (optional)
        Section section = null;
        String sectionName = null;
        if (grade != null && request.getSectionId() > 0) {
            section = sectionRepository.findById(request.getSectionId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Section not found with id: " + request.getSectionId() + " in grade " + gradeName));
            sectionName = section.getName();
        }

        // 🔹 File upload logic (same as before)
        List<String> fileUrls = new ArrayList<>();
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                try {
                    String uploadDir = "uploads/assignments/";
                    File dir = new File(uploadDir);
                    if (!dir.exists()) dir.mkdirs();

                    String filePath = uploadDir + UUID.randomUUID() + "_" + file.getOriginalFilename();
                    file.transferTo(new File(filePath));
                    fileUrls.add(filePath);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to store file " + file.getOriginalFilename(), e);
                }
            }
        }

        AssignmentStatus status = request.getStatus() != null ? request.getStatus() : AssignmentStatus.DRAFT;

        Assignment assignment = Assignment.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .dueDate(request.getDueDate())
                .gradeId(grade != null ? grade.getId() : 0)
                .sectionId(section != null ? section.getId() : 0)
                .gradeName(gradeName)
                .sectionName(sectionName)
                .createdByRole("TEACHER")
                .createdByUserId(userId)
                .createdAt(LocalDateTime.now())
                .attachments(fileUrls)
                .status(status)
                .build();

        Assignment saved = assignmentRepository.save(assignment);

        // 🔹 If assignment is published, map to students
        if (status == AssignmentStatus.PUBLISHED && grade != null && section != null) {
            List<Student> students = studentRepository.findByGradeNameAndSectionName(gradeName, sectionName);

            List<AssignmentSubmission> submissions = students.stream()
                    .map(student -> AssignmentSubmission.builder()
                            .assignment(saved)
                            .studentId(student.getStudentId())
                            .submissionStatus(SubmissionStatus.PENDING)
                            .submittedAt(null)
                            .fileUrl(null)
                            .build())
                    .toList();

            assignmentSubmissionRepository.saveAll(submissions);
            saved.setSubmissions(submissions);
        }
        return mapToResponse(saved);
    }

    @Override
    public List<AssignmentResponse> getAssignmentsForTeacher(String teacherId) {
        logger.info("Fetching assignments for teacher {}", teacherId);
        return assignmentRepository.findByCreatedByUserId(teacherId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AssignmentResponse> getAssignmentsForStudent(long gradeId, long sectionId) {
        logger.info("Fetching assignments for Grade {} Section {}", gradeId, sectionId);
        return assignmentRepository.findByGradeIdAndSectionId(gradeId, sectionId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AssignmentSubmissionResponse> getSubmissions(Long assignmentId) {
        logger.info("Fetching submissions for assignment {}", assignmentId);
        return submissionRepository.findByAssignmentId(assignmentId)
                .stream()
                .map(this::mapSubmissionToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AssignmentSubmissionResponse updateSubmissionStatus(Long assignmentId, String studentId,
                                                               SubmissionStatus submissionStatus, Double marks, String feedback) {
        logger.info("Updating submission status for assignment {} and student {}", assignmentId, studentId);

        // 1. Find submission
        AssignmentSubmission submission = submissionRepository
                .findByAssignmentIdAndStudentId(assignmentId, studentId)
                .orElseThrow(() -> {
                    logger.error("Submission not found for assignment {} student {}", assignmentId, studentId);
                    return new ResourceNotFoundException("Submission not found for this assignment and student");
                });

        // 2. Update fields
        if (marks != null) {
            submission.setMarks(marks);
        }
        if (feedback != null && !feedback.trim().isEmpty()) {
            submission.setFeedback(feedback.trim());
        }

        // Teacher updating marks/feedback implies it's evaluated
        submission.setSubmissionStatus(submissionStatus);

        // 👇 Update submittedAt timestamp
        submission.setSubmittedAt(LocalDateTime.now());

        AssignmentSubmission saved = submissionRepository.save(submission);

        logger.info("Successfully updated submission {} for assignment {} student {}", saved.getId(), assignmentId, studentId);

        // 3. Convert to response
        return AssignmentSubmissionResponse.builder()
                .id(saved.getId())
                .assignmentId(saved.getAssignment().getId())
                .studentId(saved.getStudentId())
                .fileUrl(saved.getFileUrl())
                .textAnswer(saved.getTextAnswer())
                .marks(saved.getMarks())
                .feedback(saved.getFeedback())
                .submissionStatus(saved.getSubmissionStatus())
                .submittedAt(saved.getSubmittedAt())
                .build();
    }


    @Override
    public AssignmentResponse getAssignmentById(Long id) {
        logger.info("Fetching assignment with id={}", id);
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Assignment not found for id={}", id);
                    return new ResourceNotFoundException("Assignment not found with id: " + id);
                });

        return mapToResponse(assignment);
    }

    @Override
    public void deleteAssignment(Long id) {
        logger.info("Deleting assignment with id {}", id);

        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + id));

        assignmentRepository.delete(assignment);

        logger.info("Successfully deleted assignment with id {}", id);
    }

    @Override
    @Transactional
    public AssignmentResponse updateAssignment(Long id, UpdateAssignmentRequest request,
                                               List<MultipartFile> files, String userId) {
        logger.info("Updating assignment ID {} by user {}", id, userId);

        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Assignment with ID {} not found", id);
                    return new ResourceNotFoundException("Assignment not found with id: " + id);
                });

        // Allow ADMIN or creator to update
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !assignment.getCreatedByUserId().equals(userId)) {
            logger.error("User {} attempted to update assignment {} without permission", userId, id);
            throw new AccessDeniedException("You are not allowed to update this assignment");
        }

        // 🔹 Validate due date
        if (request.getDueDate() != null && !request.getDueDate().isAfter(LocalDate.now())) {
            logger.error("Invalid due date {} for assignment {}. Must be a future date.",
                    request.getDueDate(), id);
            throw new IllegalArgumentException("Due date must be a future date");
        }

        // Update basic fields
        if (request.getTitle() != null) assignment.setTitle(request.getTitle());
        if (request.getDescription() != null) assignment.setDescription(request.getDescription());
        if (request.getDueDate() != null) assignment.setDueDate(request.getDueDate());
        if (request.getTeacherId() != null) assignment.setAssignedTeacherId(request.getTeacherId());

        // 🔹 Update grade & section properly
        if (request.getGradeId() != null) {
            Grade grade = gradeRepository.findById(request.getGradeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Grade not found with ID: " + request.getGradeId()));
            assignment.setGradeId(grade.getId());
            assignment.setGradeName(grade.getName());

            if (request.getSectionId() != null) {
                Section section = sectionRepository.findById(request.getSectionId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Section not found with ID: " + request.getSectionId()
                        ));

                if (!section.getGrade().getId().equals(grade.getId())) {
                    throw new IllegalArgumentException("Section " + section.getName() +
                            " does not belong to Grade " + grade.getName());
                }
                assignment.setSectionId(section.getId());
                assignment.setSectionName(section.getName());
            } else {
                assignment.setSectionId(0);
                assignment.setSectionName(null);
            }
        }

        // Handle file uploads (append new files if any)
        if (files != null && !files.isEmpty()) {
            List<String> fileUrls = new ArrayList<>(assignment.getAttachments() != null
                    ? assignment.getAttachments() : new ArrayList<>());

            for (MultipartFile file : files) {
                try {
                    String uploadDir = "uploads/assignments/";
                    File dir = new File(uploadDir);
                    if (!dir.exists()) dir.mkdirs();

                    String filePath = uploadDir + UUID.randomUUID() + "_" + file.getOriginalFilename();
                    file.transferTo(new File(filePath));
                    fileUrls.add(filePath);

                    logger.info("File '{}' uploaded successfully for assignment {}", file.getOriginalFilename(), id);
                } catch (Exception e) {
                    logger.error("Error saving file '{}' for assignment {}", file.getOriginalFilename(), id, e);
                    throw new RuntimeException("Failed to store file " + file.getOriginalFilename(), e);
                }
            }
            assignment.setAttachments(fileUrls);
        }

        // 🔹 Update status
        AssignmentStatus status = request.getStatus() != null ? request.getStatus() : AssignmentStatus.DRAFT;
        assignment.setStatus(status);
        assignment.setUpdatedAt(LocalDateTime.now());

        // 🔹 Update lastUpdatedBy with full name
        String fullName = fetchFullNameByUserId(userId, isAdmin);
        assignment.setLastUpdatedBy(fullName);

        Assignment updated = assignmentRepository.save(assignment);

        // 🔹 If assignment is published → create mappings for all students in grade/section
        if (status == AssignmentStatus.PUBLISHED) {
            List<Student> students;
            if (updated.getSectionId() != 0) {
                students = studentRepository.findByGradeIdAndSectionId(updated.getGradeId(), updated.getSectionId());
            } else {
                students = studentRepository.findByGradeId(updated.getGradeId());
            }

            List<AssignmentSubmission> mappings = students.stream()
                    .map(student -> AssignmentSubmission.builder()
                            .assignment(updated)
                            .studentId(student.getStudentId())
                            .submissionStatus(SubmissionStatus.PENDING)
                            .build())
                    .collect(Collectors.toList());

            assignmentSubmissionRepository.saveAll(mappings);
            logger.info("Mapped assignment {} to {} students after update", updated.getId(), students.size());
        }

        logger.info("Assignment ID {} updated successfully by {}", updated.getId(), fullName);
        return mapToResponse(updated);
    }


    @Override
    @Transactional
    public AssignmentResponse closeAssignment(Long id, String teacherId) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));

        if (!assignment.getCreatedByUserId().equals(teacherId)) {
            throw new AccessDeniedException("You are not allowed to close this assignment");
        }

        assignment.setStatus(AssignmentStatus.CLOSED);
        assignment.setUpdatedAt(LocalDateTime.now());

        return mapToResponse(assignmentRepository.save(assignment));
    }

    @Override
    public List<AssignmentResponse> getAllAssignmentsAdmin() {
        List<Assignment> allAssignments = assignmentRepository.findAll();

        return allAssignments.stream()
                .map(a -> {
                    // Fetch teacher name by username
                    String teacherName = teacherRepository.findByUsername(a.getCreatedByUserId())
                            .map(Teacher::getFullName)
                            .orElse(a.getCreatedByUserId());

                    return AssignmentResponse.builder()
                            .id(a.getId())
                            .title(a.getTitle())
                            .description(a.getDescription())
                            .dueDate(a.getDueDate())
                            .gradeName(a.getGradeName())
                            .sectionName(a.getSectionName())
                            .createdByTeacherId(teacherName)
                            .createdAt(a.getCreatedAt())
                            .updatedAt(a.getUpdatedAt())
                            .attachments(a.getAttachments() != null ? a.getAttachments() : List.of())
                            .status(a.getStatus().name())
                            .adminRemarks(a.getAdminRemarks())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AssignmentResponse updateAdminRemarks(Long assignmentId, String remarks) {
        logger.info("Admin updating remarks for assignment with id {}", assignmentId);

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> {
                    logger.error("Assignment not found with id {}", assignmentId);
                    return new ResourceNotFoundException("Assignment not found with id " + assignmentId);
                });

        assignment.setAdminRemarks(remarks);
        assignment.setUpdatedAt(LocalDateTime.now());
        assignmentRepository.save(assignment);

        logger.info("Updated admin remarks for assignment id={} successfully", assignmentId);

        // Convert to DTO
        return AssignmentResponse.builder()
                .id(assignment.getId())
                .title(assignment.getTitle())
                .description(assignment.getDescription())
                .gradeName(assignment.getGradeName())
                .sectionName(assignment.getSectionName())
                .createdByTeacherId(assignment.getCreatedByUserId())
                .createdAt(assignment.getCreatedAt())
                .updatedAt(assignment.getUpdatedAt())
                .status(assignment.getStatus().name())
                .adminRemarks(assignment.getAdminRemarks())
                .attachments(assignment.getAttachments())
                .build();
    }

    @Override
    @Transactional
    public AssignmentResponse createAssignmentAsAdmin(CreateAssignmentRequest request,
                                                      List<MultipartFile> files,
                                                      String adminId) {

        // 🔹 Resolve Grade (optional)
        Grade grade = null;
        String gradeName;
        if (request.getGradeId() > 0) {
            grade = gradeRepository.findById(request.getGradeId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Grade not found with id: " + request.getGradeId()));
            gradeName = grade.getName();
        } else {
            gradeName = null;
        }

        // 🔹 Resolve Section (optional, must belong to grade)
        Section section = null;
        String sectionName = null;
        if (grade != null && request.getSectionId() > 0) {
            section = sectionRepository.findById(request.getSectionId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Section not found with id: " + request.getSectionId() + " in grade " + gradeName));
            sectionName = section.getName();
        }

        // 🔹 Resolve Teacher (optional, only if admin assigns directly to one teacher)
        Teacher assignedTeacher = null;
        String assignedTeacherId = null;
        if (request.getTeacherId() != null && !request.getTeacherId().isBlank()) {
            assignedTeacher = teacherRepository.findByEmpId(request.getTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Teacher not found with empId: " + request.getTeacherId()));
            assignedTeacherId = assignedTeacher.getEmpId();
        }

        // 🔹 Handle File Uploads
        List<String> fileUrls = new ArrayList<>();
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                try {
                    String uploadDir = "uploads/assignments/";
                    File dir = new File(uploadDir);
                    if (!dir.exists()) dir.mkdirs();

                    String filePath = uploadDir + UUID.randomUUID() + "_" + file.getOriginalFilename();
                    file.transferTo(new File(filePath));
                    fileUrls.add(filePath);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to store file " + file.getOriginalFilename(), e);
                }
            }
        }

        // 🔹 Default assignment status (if null, fallback to DRAFT)
        AssignmentStatus status = request.getStatus() != null ? request.getStatus() : AssignmentStatus.DRAFT;

        // 🔹 Build and Save Assignment
        Assignment assignment = Assignment.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .dueDate(request.getDueDate())
                .gradeId(grade != null ? grade.getId() : 0)
                .sectionId(section != null ? section.getId() : 0)
                .gradeName(gradeName)
                .sectionName(sectionName)
                .createdByRole("ADMIN")
                .createdByUserId(adminId)
                .assignedTeacherId(assignedTeacherId)
                .createdAt(LocalDateTime.now())
                .attachments(fileUrls)
                .status(status)
                .build();

        Assignment saved = assignmentRepository.save(assignment);

        // 🔹 If assignment is published, map to students
        if (status == AssignmentStatus.PUBLISHED && grade != null && section != null) {
            List<Student> students = studentRepository.findByGradeNameAndSectionName(gradeName, sectionName);

            List<AssignmentSubmission> submissions = students.stream()
                    .map(student -> AssignmentSubmission.builder()
                            .assignment(saved)
                            .studentId(student.getStudentId())
                            .submissionStatus(SubmissionStatus.PENDING)
                            .submittedAt(null)
                            .fileUrl(null)
                            .build())
                    .toList();

            assignmentSubmissionRepository.saveAll(submissions);
            saved.setSubmissions(submissions);
        }

        return mapToResponse(saved);
    }

    private AssignmentResponse mapToResponse(Assignment assignment) {
        AssignmentResponse response = new AssignmentResponse();
        response.setId(assignment.getId());
        response.setTitle(assignment.getTitle());
        response.setDescription(assignment.getDescription());
        response.setDueDate(assignment.getDueDate());
        response.setCreatedAt(assignment.getCreatedAt());
        response.setUpdatedAt(assignment.getUpdatedAt());
        response.setLastUpdatedBy(assignment.getLastUpdatedBy());
        response.setStatus(assignment.getStatus().name());
        response.setCreatedByTeacherId(assignment.getCreatedByUserId());
        response.setGradeName(assignment.getGradeName());
        response.setSectionName(assignment.getSectionName());
        response.setAdminRemarks(assignment.getAdminRemarks());

        /*Grade grade = gradeRepository.findById(assignment.getGradeId()).orElse(null);
        Section section = sectionRepository.findById(assignment.getSectionId()).orElse(null);

        response.setGradeName(grade != null ? grade.getName() : null);
        response.setSectionName(section != null ? section.getName() : null);*/

        return response;
    }

    private AssignmentSubmissionResponse mapSubmissionToResponse(AssignmentSubmission submission) {
        return AssignmentSubmissionResponse.builder()
                .id(submission.getId())
                .studentId(submission.getStudentId())
                .submittedAt(submission.getSubmittedAt())
                .fileUrl(submission.getFileUrl())
                .textAnswer(submission.getTextAnswer())
                .marks(submission.getMarks())
                .feedback(submission.getFeedback())
                .submissionStatus(submission.getSubmissionStatus())
                .build();
    }


    /**
     * Helper method to fetch user full name from Teacher/Admin repo
     */
    private String fetchFullNameByUserId(String userId, boolean isAdmin) {
        if (isAdmin) {
            return userRepository.findByUsername(userId)
                    .map(User::getName)
                    .orElse("Unknown Admin");
        } else {
            return teacherRepository.findByUsername(userId)
                    .map(Teacher::getFullName)
                    .orElse("Unknown Teacher");
        }
    }
}
