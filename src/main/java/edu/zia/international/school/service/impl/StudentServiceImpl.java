package edu.zia.international.school.service.impl;

import edu.zia.international.school.dto.student.CreateStudentRequest;
import edu.zia.international.school.dto.student.StudentResponse;
import edu.zia.international.school.entity.*;
import edu.zia.international.school.enums.StudentStatus;
import edu.zia.international.school.exception.ResourceNotFoundException;
import edu.zia.international.school.repository.*;
import edu.zia.international.school.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class StudentServiceImpl implements StudentService {

    private static final String ROLE_NAME = "STUDENT";

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final GradeRepository gradeRepository;
    private final SectionRepository sectionRepository;
    private final StudentSerialRepository studentSerialRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final JavaMailSender javaMailSender;
    private static final Logger logger = LoggerFactory.getLogger(StudentServiceImpl.class);

    @Override
    @Transactional
    public StudentResponse createStudent(CreateStudentRequest request) {
        logger.info("Creating new student: {} {} with email: {}", request.getFirstName(), request.getLastName(), request.getEmail());

        // ✅ Check if email already exists in Student or User table
        if (studentRepository.existsByEmail(request.getEmail()) || userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists.");
        }

        // 🔹 Generate unique student username (first initial + lastname + random)
        String generatedUsername = generateUniqueStudentUsername(request.getFirstName(), request.getLastName());
        logger.info("Generated username for student: {}", generatedUsername);

        // 🔹 Generate temporary password
        String tempPassword = generateRandomPassword();
        String encodedPassword = passwordEncoder.encode(tempPassword);

        // 🔹 Create User entity for authentication
        User user = new User();
        user.setName(request.getFirstName() + " " + request.getLastName());
        user.setEmail(request.getEmail());
        user.setUsername(generatedUsername);
        user.setPassword(encodedPassword);
        user.setRole(roleRepository.findByName(ROLE_NAME)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found")));
        user.setAssignedRole("STUDENT");
        userRepository.save(user);
        logger.info("User created for student with ID: {}", user.getId());

        // 🔹 Optional: Validate Grade & Section
        Grade grade = null;
        Section section = null;

        if (request.getGradeName() != null && !request.getGradeName().isBlank()) {
            grade = gradeRepository.findByNameIgnoreCase(request.getGradeName())
                    .orElseThrow(() -> new ResourceNotFoundException("Grade not found: " + request.getGradeName()));

            if (request.getSectionName() != null && !request.getSectionName().isBlank()) {
                section = sectionRepository.findByGradeAndNameIgnoreCase(grade, request.getSectionName())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Section '" + request.getSectionName() + "' not found in grade " + request.getGradeName()));
            }
        }

        // 🔹 Generate studentId based on admission date & year
        String studentId = generateStudentId(request.getAdmissionDate());

        // 🔹 Create Student entity
        Student student = Student.builder()
                .studentId(studentId)
                .username(generatedUsername)
                .password(encodedPassword)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .role(ROLE_NAME)
                .phone(request.getPhone())
                .gender(request.getGender())
                .dateOfBirth(request.getDateOfBirth())
                .gradeName(grade != null ? grade.getName() : null)
                .sectionName(section != null ? section.getName() : null)
                .guardianName(request.getGuardianName())
                .guardianPhone(request.getGuardianPhone())
                .admissionDate(request.getAdmissionDate())
                .status(StudentStatus.ACTIVE)
                .grade(grade)
                .section(section)
                .user(user)
                .address(request.getAddress())
                .emergencyContactName(request.getEmergencyContactName())
                .emergencyContactPhone(request.getEmergencyContactPhone())
                .bloodGroup(request.getBloodGroup())
                .nationality(request.getNationality())
                .profileImageUrl(request.getProfileImageUrl())
                .build();

        Student savedStudent = studentRepository.save(student);
        logger.info("Student created successfully with ID: {}, username: {}, tempPassword: {}", studentId, generatedUsername, tempPassword);

        // 🔹 Send welcome email
        sendWelcomeEmail(student.getEmail(), student.getFirstName() + " " + student.getLastName(),
                generatedUsername, tempPassword, studentId);

        return mapToResponse(savedStudent);
    }


    @Override
    public List<StudentResponse> getAllStudents() {
        logger.info("Fetching all students from DB");
        List<Student> students = studentRepository.findAll();
        logger.info("Total students fetched: {}", students.size());
        return students.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public StudentResponse getStudentById(String studentId) {
        logger.info("Fetching student with ID: {}", studentId);
        Student student = studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> {
                    logger.error("Student not found with ID: {}", studentId);
                    return new ResourceNotFoundException("Student not found with ID: " + studentId);
                });
        logger.info("Student fetched successfully: {} {}", student.getFirstName(), student.getLastName());
        return mapToResponse(student);
    }

    @Override
    public StudentResponse updateStudent(String studentId, CreateStudentRequest request) {
        logger.info("Updating student with ID: {}", studentId);

        Student student = studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));

        // Update primitive fields
        student.setFirstName(request.getFirstName());
        student.setLastName(request.getLastName());
        student.setEmail(request.getEmail());
        student.setPhone(request.getPhone());
        student.setGender(request.getGender());
        student.setDateOfBirth(request.getDateOfBirth());
        student.setGuardianName(request.getGuardianName());
        student.setGuardianPhone(request.getGuardianPhone());
        student.setAdmissionDate(request.getAdmissionDate());
        student.setStatus(StudentStatus.ACTIVE);
        student.setAddress(request.getAddress());
        student.setEmergencyContactName(request.getEmergencyContactName());
        student.setEmergencyContactPhone(request.getEmergencyContactPhone());
        student.setBloodGroup(request.getBloodGroup());
        student.setNationality(request.getNationality());

        // 🔹 Resolve Grade entity
        Grade grade = null;
        if (request.getGradeName() != null && !request.getGradeName().isBlank()) {
            grade = gradeRepository.findByNameIgnoreCase(request.getGradeName())
                    .orElseThrow(() -> new ResourceNotFoundException("Grade not found: " + request.getGradeName()));
            student.setGrade(grade);
            student.setGradeName(grade.getName());
        } else {
            student.setGrade(null);
            student.setGradeName(null);
        }

        // 🔹 Resolve Section entity
        Section section = null;
        if (grade != null && request.getSectionName() != null && !request.getSectionName().isBlank()) {
            section = sectionRepository.findByGradeAndNameIgnoreCase(grade, request.getSectionName())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Section '" + request.getSectionName() + "' not found in grade " + request.getGradeName()));
            student.setSection(section);
            student.setSectionName(section.getName());
        } else {
            student.setSection(null);
            student.setSectionName(null);
        }

        Student updatedStudent = studentRepository.save(student);
        logger.info("Student updated successfully: {} {}", updatedStudent.getFirstName(), updatedStudent.getLastName());

        return mapToResponse(updatedStudent);
    }


    @Override
    public void deleteStudent(String studentId) {
        logger.info("Attempting to delete student with studentId: {}", studentId);

        // Fetch the student
        Student student = studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> {
                    logger.error("Student not found for delete with ID: {}", studentId);
                    return new ResourceNotFoundException("Student not found with ID: " + studentId);
                });

        // Delete associated User first if exists
        if (student.getUser() != null) {
            logger.info("Deleting associated user with username: {}", student.getUser().getUsername());
            userRepository.delete(student.getUser());
            logger.info("Deleted user: {}", student.getUser().getUsername());
        }

        // Delete the student
        studentRepository.delete(student);
        logger.info("Deleted student successfully with studentId: {}", studentId);
    }


    @Override
    public StudentResponse uploadProfileImage(String studentId, MultipartFile imageFile) {
        log.debug("Starting image upload process for studentId: {}", studentId);

        Student student = studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> {
                    log.warn("Student not found for studentId: {}", studentId);
                    return new ResourceNotFoundException("Student not found with studentId: " + studentId);
                });

        if (imageFile.isEmpty()) {
            log.error("Uploaded file is empty for studentId: {}", studentId);
            throw new IllegalArgumentException("Uploaded image is empty.");
        }

        try {
            String uploadsDir = "uploads/";
            String originalFilename = StringUtils.cleanPath(imageFile.getOriginalFilename());
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = studentId + "_profile" + fileExtension;

            Path uploadPath = Paths.get(uploadsDir);
            if (!Files.exists(uploadPath)) {
                log.info("Creating upload directory at: {}", uploadPath.toAbsolutePath());
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(filename);
            Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            student.setProfileImageUrl(filename);
            log.info("Image saved at: {} for studentId: {}", filePath.toAbsolutePath(), studentId);

            Student updatedStudent = studentRepository.save(student);
            log.debug("Student entity updated with image URL for studentId: {}", studentId);

            StudentResponse response = new StudentResponse();
            BeanUtils.copyProperties(updatedStudent, response);

            return response;

        } catch (IOException e) {
            log.error("Failed to upload image for studentId: {}", studentId, e);
            throw new RuntimeException("Failed to upload image", e);
        }
    }

    @Override
    public StudentResponse getStudentByUsername(String username) {
        Student student = studentRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found with username: " + username));

        return mapToResponse(student);
    }

    @Override
    public List<StudentResponse> getStudentsByGradeAndSection(String gradeName, String sectionName) {
        logger.info("Service: Fetching students for Grade '{}' and Section '{}'", gradeName, sectionName);

        List<Student> students;

        if (sectionName == null || sectionName.isEmpty()) {
            // Section not provided, fetch all students for the grade
            students = studentRepository.findByGradeName(gradeName);
            logger.info("No section provided. Fetching all students for grade '{}'", gradeName);
        } else {
            // Section provided, fetch specific grade + section
            students = studentRepository.findByGradeNameAndSectionName(gradeName, sectionName);
        }

        List<StudentResponse> responses = students.stream().map(student -> StudentResponse.builder()
                .id(student.getId())
                .studentId(student.getStudentId())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .email(student.getEmail())
                .phone(student.getPhone())
                .gender(student.getGender())
                .dateOfBirth(student.getDateOfBirth().toString())
                .gradeName(student.getGradeName())
                .sectionName(student.getSectionName())
                .address(student.getAddress())
                .emergencyContactName(student.getEmergencyContactName())
                .emergencyContactPhone(student.getEmergencyContactPhone())
                .bloodGroup(student.getBloodGroup())
                .nationality(student.getNationality())
                .profileImageUrl(student.getProfileImageUrl())
                .username(student.getUsername())
                .status(student.getStatus().name())
                .guardianName(student.getGuardianName())
                .guardianPhone(student.getGuardianPhone())
                .admissionDate(student.getAdmissionDate())
                .build()
        ).collect(Collectors.toList());

        logger.info("Service: Total students fetched: {}", responses.size());
        return responses;
    }

    // ---------------- Utility Methods ----------------

    // ✅ Safe studentId generation
    private String generateStudentId(LocalDate admissionDate) {
        int year = admissionDate.getYear();

        StudentSerial serial = studentSerialRepository.findByYear(year)
                .orElseGet(() -> {
                    StudentSerial newSerial = new StudentSerial();
                    newSerial.setYear(year);
                    newSerial.setLastSerial(0);
                    return studentSerialRepository.save(newSerial);
                });

        int nextSerial = serial.getLastSerial() + 1;
        serial.setLastSerial(nextSerial);
        studentSerialRepository.save(serial);

        // Format: STD20250001
        return String.format("STD%d%04d", year, nextSerial);
    }


    private String generateUniqueStudentUsername(String firstName, String lastName) {
        int random = new Random().nextInt(900) + 100; // 100-999
        return (firstName.charAt(0) + lastName).toLowerCase() + random;
    }


    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private void sendWelcomeEmail(String toEmail, String fullName, String username, String tempPassword, String studentId) {
        String subject = "Welcome to ZIA International School";
        String body = String.format("""
        Dear %s,

        Welcome to ZIA International School!

        Your student account has been created successfully. Please find your login credentials below:

        👤 Username: %s
        🔐 Temporary Password: %s
        🆔 Student ID: %s

        You can log in to the system and reset your password using the 'Forgot Password' option.

        Login Portal: http://localhost:3000/login

        Regards,
        ZIA International School Admin
        """, fullName, username, tempPassword, studentId);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            javaMailSender.send(message);
            logger.info("Welcome email sent successfully to {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send welcome email to {}: {}", toEmail, e.getMessage());
        }
    }


    private StudentResponse mapToResponse(Student student) {
        StudentResponse response = StudentResponse.builder()
                .id(student.getId())
                .studentId(student.getStudentId())
                .username(student.getUsername())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .email(student.getEmail())
                .phone(student.getPhone())
                .gender(student.getGender())
                .dateOfBirth(student.getDateOfBirth())
                .address(student.getAddress())
                .emergencyContactName(student.getEmergencyContactName())
                .emergencyContactPhone(student.getEmergencyContactPhone())
                .bloodGroup(student.getBloodGroup())
                .nationality(student.getNationality())
                .profileImageUrl(student.getProfileImageUrl())
                .guardianName(student.getGuardianName())
                .guardianPhone(student.getGuardianPhone())
                .admissionDate(student.getAdmissionDate())
                .status(student.getStatus() != null ? student.getStatus().name() : null)
                .build();

        // Set grade and section names if the entities exist
        if (student.getGrade() != null) {
            response.setGradeName(student.getGrade().getName());
        }

        if (student.getSection() != null) {
            response.setSectionName(student.getSection().getName());
        }

        return response;
    }


}
