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
    private static final Logger logger = LoggerFactory.getLogger(AssignmentServiceImpl.class);

    @Override
    @Transactional
    public AssignmentResponse createAssignment(CreateAssignmentRequest request, List<MultipartFile> files, String teacherId) {
        logger.info("Creating assignment '{}' for Grade {} Section {}",
                request.getTitle(), request.getGradeId(), request.getSectionId());

        // 🔹 Validate due date
        if (request.getDueDate() == null) {
            logger.error("Due date is missing for assignment '{}'", request.getTitle());
            throw new IllegalArgumentException("Due date cannot be null");
        }
        if (!request.getDueDate().isAfter(LocalDate.now())) {
            logger.error("Invalid due date {} for assignment '{}'. Must be a future date.",
                    request.getDueDate(), request.getTitle());
            throw new IllegalArgumentException("Due date must be a future date");
        }

        // 🔹 Determine status
        AssignmentStatus status = request.getStatus() != null ? request.getStatus() : AssignmentStatus.DRAFT;


        List<String> fileUrls = new ArrayList<>();

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                try {
                    // 🔹 For now: store locally (later replace with S3 upload)
                    String uploadDir = "uploads/assignments/";
                    File dir = new File(uploadDir);
                    if (!dir.exists()) dir.mkdirs();

                    String filePath = uploadDir + UUID.randomUUID() + "_" + file.getOriginalFilename();
                    file.transferTo(new File(filePath));

                    // Save relative path or full URL (later S3 URL)
                    fileUrls.add(filePath);

                    logger.info("File '{}' uploaded successfully", file.getOriginalFilename());
                } catch (Exception e) {
                    logger.error("Error saving file: {}", file.getOriginalFilename(), e);
                    throw new RuntimeException("Failed to store file " + file.getOriginalFilename(), e);
                }
            }
        }

        Assignment assignment = Assignment.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .dueDate(request.getDueDate())
                .gradeId(request.getGradeId())
                .sectionId(request.getSectionId())
                .createdByTeacherId(teacherId)
                .createdAt(LocalDateTime.now())
                .attachments(fileUrls)
                .status(status)
                .build();

        Assignment saved = assignmentRepository.save(assignment);

        logger.info("Assignment '{}' created successfully with ID {}", saved.getTitle(), saved.getId());
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

        Grade grade = gradeRepository.findById(assignment.getGradeId()).orElse(null);
        Section section = sectionRepository.findById(assignment.getSectionId()).orElse(null);

        response.setGradeName(grade != null ? grade.getName() : null);
        response.setSectionName(section != null ? section.getName() : null);

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
