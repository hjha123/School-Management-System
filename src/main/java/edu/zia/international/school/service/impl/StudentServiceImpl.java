package edu.zia.international.school.service.impl;

import edu.zia.international.school.dto.StudentDTO;
import edu.zia.international.school.entity.Student;
import edu.zia.international.school.exception.ResourceNotFoundException;
import edu.zia.international.school.repository.StudentRepository;
import edu.zia.international.school.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;

    @Override
    public StudentDTO createStudent(StudentDTO dto) {
        try {
            log.info("Creating student: {}", dto);
            Student student = new Student();
            BeanUtils.copyProperties(dto, student);
            Student saved = studentRepository.save(student);
            return toDTO(saved);
        } catch (Exception e) {
            log.error("Error creating student", e);
            throw new RuntimeException("Unable to create student");
        }
    }

    @Override
    public StudentDTO getStudentById(Long id) {
        log.info("Fetching student by ID: {}", id);
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
        return toDTO(student);
    }

    @Override
    public List<StudentDTO> getAllStudents() {
        try {
            log.info("Fetching all students");
            return studentRepository.findAll()
                    .stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching all students", e);
            throw new RuntimeException("Unable to fetch students");
        }
    }

    @Override
    public StudentDTO updateStudent(Long id, StudentDTO dto) {
        try {
            log.info("Updating student with ID {}: {}", id, dto);
            Student student = studentRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
            BeanUtils.copyProperties(dto, student, "id");
            return toDTO(studentRepository.save(student));
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error updating student with ID {}", id, e);
            throw new RuntimeException("Unable to update student");
        }
    }


    @Override
    public void deleteStudent(Long id) {
        log.info("Deleting student with ID {}", id);

        if (!studentRepository.existsById(id)) {
            log.warn("Student with ID {} not found", id);
            throw new ResourceNotFoundException("Student with ID " + id + " not found");
        }

        studentRepository.deleteById(id);
        log.info("Successfully deleted student with ID {}", id);
    }

    private StudentDTO toDTO(Student student) {
        StudentDTO dto = new StudentDTO();
        BeanUtils.copyProperties(student, dto);
        return dto;
    }
}

