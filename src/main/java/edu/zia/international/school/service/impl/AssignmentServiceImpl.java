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
    private final AssignmentSubmissionRepository submissionRepository;
    private final GradeRepository gradeRepository;
    private final SectionRepository sectionRepository;
    private final TeacherRepository teacherRepository;
    private static final Logger logger = LoggerFactory.getLogger(AssignmentServiceImpl.class);

    @Override
    @Transactional
    public AssignmentResponse createAssignment(CreateAssignmentRequest request, List<MultipartFile> files, String teacherId) {

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
                .createdByTeacherId(teacherId)
                .createdAt(LocalDateTime.now())
                .attachments(fileUrls)
                .status(status)
                .build();

        Assignment saved = assignmentRepository.save(assignment);
        return mapToResponse(saved);
    }

    @Override
    public List<AssignmentResponse> getAssignmentsForTeacher(String teacherId) {
        logger.info("Fetching assignments for teacher {}", teacherId);
        return assignmentRepository.findByCreatedByTeacherId(teacherId)
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
    public AssignmentSubmissionResponse updateSubmissionStatus(Long assignmentId, String studentId, Double marks, String feedback) {
        logger.info("Updating submission status for assignment {} student {}", assignmentId, studentId);
        AssignmentSubmission submission = submissionRepository.findByAssignmentIdAndStudentId(assignmentId, studentId);
        if (submission == null) {
            throw new RuntimeException("Submission not found for student " + studentId);
        }

        submission.setMarks(marks);
        submission.setFeedback(feedback);
        submission.setStatus(SubmissionStatus.GRADED);

        AssignmentSubmission updated = submissionRepository.save(submission);
        logger.info("Submission updated successfully for student {}", studentId);
        return mapSubmissionToResponse(updated);
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
                                               List<MultipartFile> files, String teacherId) {
        logger.info("Updating assignment ID {} by teacher {}", id, teacherId);

        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Assignment with ID {} not found", id);
                    return new ResourceNotFoundException("Assignment not found with id: " + id);
                });

        // Ensure only the creator can update
        if (!assignment.getCreatedByTeacherId().equals(teacherId)) {
            logger.error("Teacher {} attempted to update assignment {} created by another teacher", teacherId, id);
            throw new AccessDeniedException("You are not allowed to update this assignment");
        }

        // 🔹 Validate due date
        if (request.getDueDate() != null && !request.getDueDate().isAfter(LocalDate.now())) {
            logger.error("Invalid due date {} for assignment {}. Must be a future date.",
                    request.getDueDate(), id);
            throw new IllegalArgumentException("Due date must be a future date");
        }


        // Update fields
        if (request.getTitle() != null) assignment.setTitle(request.getTitle());
        if (request.getDescription() != null) assignment.setDescription(request.getDescription());
        if (request.getDueDate() != null) assignment.setDueDate(request.getDueDate());
        if (request.getGradeId() != null) assignment.setGradeId(request.getGradeId());
        if (request.getSectionId() != null) assignment.setSectionId(request.getSectionId());

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

        AssignmentStatus status = request.getStatus() != null ? request.getStatus() : AssignmentStatus.DRAFT;
        assignment.setStatus(status);
        assignment.setUpdatedAt(LocalDateTime.now());
        Assignment updated = assignmentRepository.save(assignment);

        logger.info("Assignment ID {} updated successfully", updated.getId());
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public AssignmentResponse closeAssignment(Long id, String teacherId) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));

        if (!assignment.getCreatedByTeacherId().equals(teacherId)) {
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
                    String teacherName = teacherRepository.findByUsername(a.getCreatedByTeacherId())
                            .map(Teacher::getFullName)
                            .orElse(a.getCreatedByTeacherId());

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
                .createdByTeacherId(assignment.getCreatedByTeacherId())
                .createdAt(assignment.getCreatedAt())
                .updatedAt(assignment.getUpdatedAt())
                .status(assignment.getStatus().name())
                .adminRemarks(assignment.getAdminRemarks())
                .attachments(assignment.getAttachments())
                .build();
    }



    private AssignmentResponse mapToResponse(Assignment assignment) {
        AssignmentResponse response = new AssignmentResponse();
        response.setId(assignment.getId());
        response.setTitle(assignment.getTitle());
        response.setDescription(assignment.getDescription());
        response.setDueDate(assignment.getDueDate());
        response.setCreatedAt(assignment.getCreatedAt());
        response.setUpdatedAt(assignment.getUpdatedAt());
        response.setStatus(assignment.getStatus().name());
        response.setCreatedByTeacherId(assignment.getCreatedByTeacherId());
        response.setGradeName(assignment.getGradeName());
        response.setSectionName(assignment.getSectionName());

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
                .status(submission.getStatus().name())
                .build();
    }
}
