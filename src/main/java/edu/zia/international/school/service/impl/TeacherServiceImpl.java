package edu.zia.international.school.service.impl;

import edu.zia.international.school.dto.teacher.CreateTeacherRequest;
import edu.zia.international.school.dto.teacher.TeacherResponse;
import edu.zia.international.school.dto.teacher.UpdateTeacherRequest;
import edu.zia.international.school.entity.*;
import edu.zia.international.school.enums.TeacherStatus;
import edu.zia.international.school.exception.ResourceNotFoundException;
import edu.zia.international.school.repository.*;
import edu.zia.international.school.service.TeacherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TeacherServiceImpl implements TeacherService {
    private static final String ROLE_NAME = "TEACHER";
    private static final String PREFIX_STAFF_ID = "ZIA";

    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SubjectRepository subjectRepository;
    private final GradeRepository gradeRepository;
    private final SectionRepository sectionRepository;
    private final TeacherSerialRepository teacherSerialRepository;

    private final PasswordEncoder passwordEncoder;

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

        // 🔢 Generate staff ID
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

        // ✉️ TODO: Send email with username & password (log for now)
        log.info("Send reset link email to: {} with username {} & temp password: {}", request.getEmail(), generatedUsername, tempPassword);

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

}
