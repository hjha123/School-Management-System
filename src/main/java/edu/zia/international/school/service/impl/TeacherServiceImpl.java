package edu.zia.international.school.service.impl;

import edu.zia.international.school.dto.teacher.*;
import edu.zia.international.school.entity.*;
import edu.zia.international.school.enums.TeacherStatus;
import edu.zia.international.school.exception.ResourceNotFoundException;
import edu.zia.international.school.repository.*;
import edu.zia.international.school.service.TeacherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TeacherServiceImpl implements TeacherService {
    private static final String ROLE_NAME = "TEACHER";
    private static final String PREFIX_EMPLOYEE_ID = "ZIA";

    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SubjectRepository subjectRepository;
    private final GradeRepository gradeRepository;
    private final SectionRepository sectionRepository;
    private final TeacherSerialRepository teacherSerialRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender javaMailSender;


    @Override
    public TeacherResponse createTeacher(CreateTeacherRequest request) {
        log.info("Creating new teacher {} with email: {}", request.getFullName(), request.getEmail());

        if (teacherRepository.existsByEmail(request.getEmail()) || userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists.");
        }

        // ✅ Generate unique username automatically (firstname.lastname)
        String generatedUsername = generateUniqueUsername(request.getFullName());
        log.info("Generated username for teacher: {}", generatedUsername);

        // 🔐 Generate temporary password
        String tempPassword = UUID.randomUUID().toString().substring(0, 8);

        // 🧑 Create User entity
        User user = new User();
        user.setName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setUsername(generatedUsername);
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setRole(roleRepository.findByName(ROLE_NAME)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found")));
        user.setAssignedRole(ROLE_NAME);
        userRepository.save(user);
        log.info("User created for teacher: {}", user.getId());

        // 🔍 Fetch Subject entities
        List<Subject> subjects = subjectRepository.findAllById(request.getSubjectIds());
        if (subjects.size() != request.getSubjectIds().size()) {
            throw new RuntimeException("One or more subject IDs are invalid");
        }

        // ✅ Optional: Fetch Grade and Section if provided
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

        // 🔢 Generate Emp ID
        String empId = generateEmpId();

        // 🧑‍🏫 Create Teacher entity
        Teacher teacher = Teacher.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .username(generatedUsername)
                .phone(request.getPhone())
                .gender(request.getGender())
                .dateOfBirth(request.getDateOfBirth())
                .qualification(request.getQualification())
                .address(request.getAddress())
                .joiningDate(request.getJoiningDate())
                .experienceYears(request.getExperienceYears())
                .empId(empId)
                .status(TeacherStatus.ACTIVE)
                .user(user)
                .role(ROLE_NAME)
                .subjects(subjects)
                .grade(grade)
                .section(section)
                .teacherType(request.getTeacherType())
                .build();

        Teacher saved = teacherRepository.save(teacher);
        log.info("Teacher saved with ID: {}", saved.getId());

        log.info("Sending welcome mail to user {} at email id {}", user.getName(), user.getEmail());
        sendWelcomeEmail(request.getEmail(), request.getFullName(), generatedUsername, tempPassword, empId);
        log.info("Welcome email sent to {}", request.getEmail());


        // 🔁 Prepare response
        TeacherResponse response = new TeacherResponse();
        response.setId(saved.getId());
        response.setFullName(saved.getFullName());
        response.setEmail(saved.getEmail());
        response.setUsername(saved.getUsername());
        response.setPhone(saved.getPhone());
        response.setSubjects(subjects.stream().map(Subject::getName).toList());
        response.setGender(saved.getGender());
        response.setDateOfBirth(saved.getDateOfBirth());
        response.setQualification(saved.getQualification());
        response.setAddress(saved.getAddress());
        response.setJoiningDate(saved.getJoiningDate());
        response.setExperienceYears(saved.getExperienceYears());
        response.setEmpId(saved.getEmpId());
        response.setStatus(saved.getStatus());
        response.setTeacherType(saved.getTeacherType());

        if (grade != null) response.setGradeName(grade.getName());
        if (section != null) response.setSectionName(section.getName());

        return response;
    }

    @Override
    public List<TeacherResponse> getAllTeachers() {
        log.info("Fetching all teachers...");
        return teacherRepository.findAll().stream().map(teacher -> {
            TeacherResponse res = new TeacherResponse();
            BeanUtils.copyProperties(teacher, res);

            // Set subject names
            res.setSubjects(
                    teacher.getSubjects().stream()
                            .map(Subject::getName)
                            .collect(Collectors.toList())
            );

            // ✅ Set grade and section if present
            if (teacher.getGrade() != null) {
                res.setGradeName(teacher.getGrade().getName());
            }
            if (teacher.getSection() != null) {
                res.setSectionName(teacher.getSection().getName());
            }

            return res;
        }).collect(Collectors.toList());
    }


    @Override
    public TeacherResponse getTeacherById(Long id) {
        log.info("Fetching teacher with ID: {}", id);
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        TeacherResponse res = new TeacherResponse();
        BeanUtils.copyProperties(teacher, res);
        res.setSubjects(teacher.getSubjects().stream()
                .map(Subject::getName).collect(Collectors.toList()));
        return res;
    }

    @Override
    public TeacherResponse getByEmpId(String empId) {
        log.info("Fetching teacher with Employee ID: {}", empId);

        Teacher teacher = teacherRepository.findByEmpId(empId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with Employee ID:: " + empId));

        TeacherResponse res = new TeacherResponse();
        BeanUtils.copyProperties(teacher, res);
        if(teacher.getGrade() != null){
            res.setGradeName(teacher.getGrade().getName());
            if(Objects.nonNull(teacher.getGrade().getSections())){
                Optional<String> sectionName = teacher.getGrade().getSections().stream().map(section -> section.getName()).findFirst();
                res.setSectionName(sectionName.isPresent() ? sectionName.get() : null);
            }
        }

        return res;
    }


    @Override
    @Transactional
    public TeacherResponse updateTeacher(Long id, UpdateTeacherRequest request) {
        log.info("Updating teacher with ID: {}", id);

        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + id));

        // Update basic fields
        teacher.setFullName(request.getFullName());
        teacher.setPhone(request.getPhone());
        teacher.setGender(request.getGender());
        teacher.setDateOfBirth(request.getDateOfBirth());
        teacher.setQualification(request.getQualification());
        teacher.setAddress(request.getAddress());
        teacher.setJoiningDate(request.getJoiningDate());
        teacher.setExperienceYears(request.getExperienceYears());

        // Sync name with associated User
        if (teacher.getUser() != null) {
            teacher.getUser().setName(request.getFullName());
        }

        // Update subjects
        List<Subject> subjects = subjectRepository.findAllById(request.getSubjectIds());
        if (subjects.size() != request.getSubjectIds().size()) {
            throw new RuntimeException("One or more subject IDs are invalid");
        }
        teacher.setSubjects(subjects);

        // Update grade and section if provided
        if (request.getGradeName() != null && !request.getGradeName().isBlank()) {
            Grade grade = gradeRepository.findByName(request.getGradeName())
                    .orElseThrow(() -> new ResourceNotFoundException("Grade not found: " + request.getGradeName()));
            teacher.setGrade(grade);

            if (request.getSectionName() != null && !request.getSectionName().isBlank()) {
                Section section = sectionRepository.findByNameAndGrade(request.getSectionName(), grade)
                        .orElseThrow(() -> new ResourceNotFoundException("Section '" + request.getSectionName()
                                + "' not found in Grade '" + grade.getName() + "'"));
                teacher.setSection(section);
            } else {
                teacher.setSection(null); // Clear section if not provided
            }
        } else {
            // Clear both grade and section if grade is not provided
            teacher.setGrade(null);
            teacher.setSection(null);
        }

        Teacher updated = teacherRepository.save(teacher);
        log.info("Teacher updated with ID: {}", updated.getId());

        // Prepare response
        TeacherResponse response = new TeacherResponse();
        response.setId(updated.getId());
        response.setFullName(updated.getFullName());
        response.setUsername(updated.getUsername());
        response.setEmail(updated.getEmail());
        response.setPhone(updated.getPhone());
        response.setGender(updated.getGender());
        response.setDateOfBirth(updated.getDateOfBirth());
        response.setQualification(updated.getQualification());
        response.setAddress(updated.getAddress());
        response.setJoiningDate(updated.getJoiningDate());
        response.setExperienceYears(updated.getExperienceYears());
        response.setSubjects(subjects.stream().map(Subject::getName).toList());

        if (updated.getGrade() != null) {
            response.setGradeName(updated.getGrade().getName());
        }
        if (updated.getSection() != null) {
            response.setSectionName(updated.getSection().getName());
        }

        return response;
    }

    @Override
    public void deleteTeacher(Long id) {
        log.info("Deleting teacher with ID: {}", id);

        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));

        // Remove join table relations
        teacher.setSubjects(null);
        teacherRepository.save(teacher);

        // Delete related User
        if (teacher.getUser() != null) {
            userRepository.delete(teacher.getUser());
        }

        // Delete teacher
        teacherRepository.delete(teacher);
        log.info("Teacher and associated User deleted successfully.");
    }

    @Override
    public void deleteTeacherByEmpId(String empId) {
        Teacher teacher = teacherRepository.findByEmpId(empId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with empId: " + empId));

        // First, delete associated User if present
        if (teacher.getUser() != null) {
            log.info("Deleting associated user with teacher's empId: {}", teacher.getUser().getUsername());
            userRepository.delete(teacher.getUser());
            log.info("Deleted user {}", teacher.getUser().getUsername());
        }

        // Then delete the teacher
        teacherRepository.delete(teacher);

        log.info("Deleted teacher with empId: {}", empId);
    }

    @Override
    @Transactional
    public TeacherResponse updateTeacherByEmpId(String empId, UpdateTeacherRequest request) {
        Teacher teacher = teacherRepository.findByEmpId(empId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with empId: " + empId));

        log.info("Updating teacher with empId: {}", empId);

        // Update simple fields
        teacher.setFullName(request.getFullName());
        teacher.setPhone(request.getPhone());
        teacher.setGender(request.getGender());
        teacher.setDateOfBirth(request.getDateOfBirth());
        teacher.setQualification(request.getQualification());
        teacher.setAddress(request.getAddress());
        teacher.setJoiningDate(request.getJoiningDate());
        teacher.setExperienceYears(request.getExperienceYears());
        teacher.setTeacherType(request.getTeacherType());
        teacher.setMaritalStatus(request.getMaritalStatus());
        teacher.setEmergencyContactInfo(request.getEmergencyContactInfo());
        teacher.setBloodGroup(request.getBloodGroup());
        teacher.setNationality(request.getNationality());
        teacher.setAadharNumber(normalizeEmptyToNull(request.getAadharNumber()));
        teacher.setProfileImageUrl(request.getProfileImageUrl());
        teacher.setEmail(request.getEmail());

        // Update User email if changed
        User user = userRepository.findByUsername(teacher.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found for teacher empId: " + empId));

        if (!user.getEmail().equals(request.getEmail())) {
            log.info("Updating user email from {} to {}", user.getEmail(), request.getEmail());
            user.setEmail(request.getEmail());
            userRepository.save(user);
        }

        // Update Subjects
        if (request.getSubjectIds() != null && !request.getSubjectIds().isEmpty()) {
            List<Subject> subjects = subjectRepository.findAllById(request.getSubjectIds());
            teacher.setSubjects(new ArrayList<>(subjects));
        } else {
            teacher.setSubjects(null);
        }

        // Update Grade if provided
        String gradeName = "";
        String sectionName = "";
        if (request.getGradeName() != null && !request.getGradeName().isBlank()) {
            Grade grade = gradeRepository.findByName(request.getGradeName())
                    .orElseThrow(() -> new ResourceNotFoundException("Grade not found with name: " + request.getGradeName()));
            teacher.setGrade(grade);
            gradeName = grade.getName();

            // If section is also provided, validate section under grade
            if (request.getSectionName() != null && !request.getSectionName().isBlank()) {
                Section section = sectionRepository.findByNameAndGrade(request.getSectionName(), grade)
                        .orElseThrow(() -> new ResourceNotFoundException("Section '" + request.getSectionName()
                                + "' not found under Grade '" + request.getGradeName() + "'"));
                teacher.setSection(section);
                sectionName = section.getName();
            } else {
                teacher.setSection(null);
            }
        } else {
            if (request.getSectionName() == null) {
                teacher.setSection(null);
            }
        }

        Teacher updatedTeacher = teacherRepository.save(teacher);
        log.info("Teacher with empId {} updated successfully", empId);

        TeacherResponse res = new TeacherResponse();
        BeanUtils.copyProperties(updatedTeacher, res);
        res.setSubjects(updatedTeacher.getSubjects().stream().map(Subject::getName).toList());
        res.setGradeName(gradeName);
        res.setSectionName(sectionName);

        return res;
    }

    private String normalizeEmptyToNull(String input) {
        return (input == null || input.trim().isEmpty()) ? null : input.trim();
    }

    private String generateUniqueUsername(String fullName) {
        String[] names = fullName.trim().toLowerCase().split("\\s+");

        String firstName = names[0];
        String lastName = names.length > 1 ? names[names.length - 1] : "";
        String baseUsername = firstName + "." + lastName;
        String username = baseUsername;

        int suffix = 1;
        while (teacherRepository.existsByUsername(username) || userRepository.existsByUsername(username)) {
            username = baseUsername + suffix;
            suffix++;
        }

        return username;
    }

    @Transactional
    public String generateEmpId() {
        int currentYear = Year.now().getValue();
        TeacherSerial serial = teacherSerialRepository.findByYear(currentYear)
                .orElseGet(() -> {
                    TeacherSerial newSerial = new TeacherSerial();
                    newSerial.setYear(currentYear);
                    newSerial.setLastSerial(0);
                    return teacherSerialRepository.save(newSerial);
                });

        // increment safely
        int nextSerial = serial.getLastSerial() + 1;
        serial.setLastSerial(nextSerial);
        teacherSerialRepository.save(serial);

        // format: ZIA2025001
        return String.format("ZIA%d%03d", currentYear, nextSerial);
    }

    @Override
    public TeacherResponse uploadProfileImage(String empId, MultipartFile imageFile) {
        log.debug("Starting image upload process for empId: {}", empId);

        Teacher teacher = teacherRepository.findByEmpId(empId)
                .orElseThrow(() -> {
                    log.warn("Teacher not found for empId: {}", empId);
                    return new ResourceNotFoundException("Teacher not found with empId: " + empId);
                });

        if (imageFile.isEmpty()) {
            log.error("Uploaded file is empty for empId: {}", empId);
            throw new IllegalArgumentException("Uploaded image is empty.");
        }

        try {
            String uploadsDir = "uploads/";
            String originalFilename = StringUtils.cleanPath(imageFile.getOriginalFilename());
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = empId + "_profile" + fileExtension;

            Path uploadPath = Paths.get(uploadsDir);
            if (!Files.exists(uploadPath)) {
                log.info("Creating upload directory at: {}", uploadPath.toAbsolutePath());
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(filename);
            Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            teacher.setProfileImageUrl(filename);
            log.info("Image saved at: {} for empId: {}", filePath.toAbsolutePath(), empId);

            Teacher updatedTeacher = teacherRepository.save(teacher);
            log.debug("Teacher entity updated with image URL for empId: {}", empId);

            TeacherResponse response = new TeacherResponse();
            BeanUtils.copyProperties(updatedTeacher, response);

            return response;

        } catch (IOException e) {
            log.error("Failed to upload image for empId: {}", empId, e);
            throw new RuntimeException("Failed to upload image", e);
        }
    }

    @Override
    public TeacherResponse getTeacherByUsername(String username) {
      log.info("Fetching teacher with username: {}", username);
        Teacher teacher = teacherRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Teacher not found with username: " + username));

        TeacherResponse res = new TeacherResponse();
        BeanUtils.copyProperties(teacher, res);
        res.setSubjects(teacher.getSubjects().stream()
                .map(Subject::getName).collect(Collectors.toList()));
        return res;
    }

    @Override
    @Transactional
    public TeacherResponse partialUpdateByUsername(String username, PartialUpdateTeacherRequest request) {
        Teacher teacher = teacherRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Teacher not found with username: " + username));

        log.info("Updating teacher with username: {}", username);

        // Update simple fields
        teacher.setExperienceYears(request.getExperienceYears());
        teacher.setMaritalStatus(request.getMaritalStatus());
        teacher.setEmergencyContactInfo(request.getEmergencyContactInfo());
        teacher.setBloodGroup(request.getBloodGroup());
        teacher.setNationality(request.getNationality());
        teacher.setAadharNumber(normalizeEmptyToNull(request.getAadharNumber()));
        teacher.setAddress(request.getAddress());


        Teacher updatedTeacher = teacherRepository.save(teacher);
        log.info("Teacher with username {} updated successfully", username);

        TeacherResponse res = new TeacherResponse();
        BeanUtils.copyProperties(updatedTeacher, res);
        return res;
    }

    @Override
    public CurrentUserResponse getEmpIdAndName(String username) {
        log.info("Fetching current logged in user emp id & name with username: {}", username);
        Teacher teacher = teacherRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Teacher not found with username: " + username));

        CurrentUserResponse res = new CurrentUserResponse();
        res.setEmployeeId(teacher.getEmpId());
        res.setFullName(teacher.getFullName());
        return res;
    }

    @Override
    public List<TeacherResponse> searchTeachers(Long gradeId, String gradeName,
                                                Long sectionId, String sectionName,
                                                String name, String empId, String teacherType) {

        List<Teacher> teachers;

        if (gradeId != null && sectionId != null) {
            teachers = teacherRepository.findByGradeIdAndSectionId(gradeId, sectionId);
        } else if (gradeId != null) {
            teachers = teacherRepository.findByGradeId(gradeId);
        } else if (sectionId != null) {
            teachers = teacherRepository.findBySectionId(sectionId);
        } else if (gradeName != null && sectionName != null) {
            teachers = teacherRepository.findByGradeNameAndSectionName(gradeName, sectionName);
        } else if (gradeName != null) {
            teachers = teacherRepository.findByGradeName(gradeName);
        } else if (sectionName != null) {
            teachers = teacherRepository.findBySectionName(sectionName);
        } else {
            teachers = teacherRepository.findAll();
        }

        // Apply in-memory filters
        if (name != null && !name.isEmpty()) {
            String lowerName = name.toLowerCase();
            teachers = teachers.stream()
                    .filter(t -> t.getFullName().toLowerCase().contains(lowerName))
                    .toList();
        }
        if (empId != null && !empId.isEmpty()) {
            teachers = teachers.stream()
                    .filter(t -> t.getEmpId() != null && t.getEmpId().equalsIgnoreCase(empId))
                    .toList();
        }
        if (teacherType != null && !teacherType.isEmpty()) {
            teachers = teachers.stream()
                    .filter(t -> t.getTeacherType() != null && t.getTeacherType().equalsIgnoreCase(teacherType))
                    .toList();
        }

        // Map to response
        return teachers.stream()
                .map(t -> {
                    TeacherResponse res = new TeacherResponse();
                    BeanUtils.copyProperties(t, res);
                    if (t.getSubjects() != null)
                        res.setSubjects(t.getSubjects().stream().map(s -> s.getName()).toList());
                    if (t.getGrade() != null) res.setGradeName(t.getGrade().getName());
                    if (t.getSection() != null) res.setSectionName(t.getSection().getName());
                    return res;
                })
                .toList();
    }



    private void sendWelcomeEmail(String toEmail, String fullName, String username, String tempPassword, String empId) {
        String subject = "Welcome to ZIA International School";
        String body = String.format("""
            Dear %s,
            
            Welcome to ZIA International School!

            Your teacher account has been created successfully. Please find your login credentials below:

            👤 Username: %s
            🔐 Temporary Password: %s
            🆔 Employee ID: %s

            You can log in to the system and reset your password using the 'Forgot Password' option.

            Login Portal: http://localhost:3000/login

            Regards,
            ZIA International School Admin
            """, fullName, username, tempPassword, empId);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        javaMailSender.send(message);
    }


}
