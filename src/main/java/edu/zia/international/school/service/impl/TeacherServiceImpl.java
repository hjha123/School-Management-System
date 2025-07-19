package edu.zia.international.school.service.impl;

import edu.zia.international.school.dto.CreateTeacherRequest;
import edu.zia.international.school.dto.TeacherResponse;
import edu.zia.international.school.dto.UpdateTeacherRequest;
import edu.zia.international.school.entity.Subject;
import edu.zia.international.school.entity.Teacher;
import edu.zia.international.school.entity.User;
import edu.zia.international.school.exception.ResourceNotFoundException;
import edu.zia.international.school.repository.RoleRepository;
import edu.zia.international.school.repository.SubjectRepository;
import edu.zia.international.school.repository.TeacherRepository;
import edu.zia.international.school.repository.UserRepository;
import edu.zia.international.school.service.TeacherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TeacherServiceImpl implements TeacherService {
    private static final String ROLE_NAME = "TEACHER";

    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SubjectRepository subjectRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public TeacherResponse createTeacher(CreateTeacherRequest request) {
        log.info("Creating teacher with email: {}", request.getEmail());

        if (teacherRepository.existsByEmail(request.getEmail()) || userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists.");
        }

        if (teacherRepository.existsByUsername(request.getUsername()) || userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists.");
        }

        // 🔐 Generate temporary password
        String tempPassword = UUID.randomUUID().toString().substring(0, 8);

        // 🧑 Create User entity
        User user = new User();
        user.setName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setRole(roleRepository.findByName(ROLE_NAME).orElseThrow(() -> new ResourceNotFoundException("Role not found")));
        userRepository.save(user);
        log.info("User created for teacher: {}", user.getId());

        // 🔍 Fetch Subject entities
        List<Subject> subjects = subjectRepository.findAllById(request.getSubjectIds());
        if (subjects.size() != request.getSubjectIds().size()) {
            throw new RuntimeException("One or more subject IDs are invalid");
        }

        // 🧑‍🏫 Create Teacher entity
        Teacher teacher = Teacher.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .username(request.getUsername())
                .phone(request.getPhone())
                .gender(request.getGender())
                .dateOfBirth(request.getDateOfBirth())
                .qualification(request.getQualification())
                .address(request.getAddress())
                .joiningDate(request.getJoiningDate())
                .experienceYears(request.getExperienceYears())
                .user(user)
                .role(ROLE_NAME)
                .subjects(subjects)
                .build();

        Teacher saved = teacherRepository.save(teacher);
        log.info("Teacher saved with ID: {}", saved.getId());

        // ✉️ TODO: Send email for password reset link (mock/log here)
        log.info("Send reset link email to: {} with temp password: {}", request.getEmail(), tempPassword);

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

        return response;
    }

    @Override
    public List<TeacherResponse> getAllTeachers() {
        log.info("Fetching all teachers...");
        return teacherRepository.findAll().stream().map(teacher -> {
            TeacherResponse res = new TeacherResponse();
            BeanUtils.copyProperties(teacher, res);
            res.setSubjects(teacher.getSubjects().stream()
                    .map(Subject::getName).collect(Collectors.toList()));
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
}
