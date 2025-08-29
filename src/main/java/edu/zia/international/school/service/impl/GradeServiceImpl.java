package edu.zia.international.school.service.impl;

import edu.zia.international.school.dto.grade.GradeRequest;
import edu.zia.international.school.dto.grade.GradeResponse;
import edu.zia.international.school.dto.grade.GradeStatsResponse;
import edu.zia.international.school.dto.grade.GradeWithSectionsResponse;
import edu.zia.international.school.dto.section.SimpleSectionResponse;
import edu.zia.international.school.entity.Grade;
import edu.zia.international.school.entity.Section;
import edu.zia.international.school.entity.Student;
import edu.zia.international.school.entity.Teacher;
import edu.zia.international.school.exception.ResourceNotFoundException;
import edu.zia.international.school.repository.GradeRepository;
import edu.zia.international.school.repository.SectionRepository;
import edu.zia.international.school.repository.StudentRepository;
import edu.zia.international.school.repository.TeacherRepository;
import edu.zia.international.school.service.GradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GradeServiceImpl implements GradeService {

    private final GradeRepository gradeRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final SectionRepository sectionRepository;

    @Override
    public GradeResponse createGrade(GradeRequest request) {
        log.info("Creating new grade: {}", request.getName());

        Grade grade = Grade.builder()
                .name(request.getName())
                .build();

        Grade saved = gradeRepository.save(grade);

        return new GradeResponse(saved.getId(), saved.getName());
    }

    @Override
    public List<GradeResponse> getAllGrades() {
        log.info("Fetching all grades");

        return gradeRepository.findAll()
                .stream()
                .map(g -> new GradeResponse(g.getId(), g.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public GradeResponse getGradeById(Long id) {
        log.info("Fetching grade with ID: {}", id);

        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found with ID: " + id));

        return new GradeResponse(grade.getId(), grade.getName());
    }


    @Override
    @Transactional
    public void deleteGradeByName(String gradeName) {
        Grade grade = gradeRepository.findByNameIgnoreCase(gradeName)
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found with name: " + gradeName));

        log.info("Deleting grade and all related sections for grade: {}", gradeName);

        // 1. Disassociate teachers
        List<Teacher> teachersInGrade = teacherRepository.findByGradeId(grade.getId());
        for (Teacher teacher : teachersInGrade) {
            teacher.setGrade(null);
            teacher.setSection(null);
            teacherRepository.save(teacher);
        }

        // 2. Disassociate students (all students in this grade)
        List<Student> studentsInGrade = studentRepository.findByGradeId(grade.getId());
        for (Student student : studentsInGrade) {
            student.setGrade(null);
            student.setSection(null);
            studentRepository.save(student);
        }

        // 3. Delete sections
        List<Section> sections = sectionRepository.findByGradeId(grade.getId());
        for (Section section : sections) {
            log.info("Deleting section: {}", section.getName());
            sectionRepository.delete(section);
        }

        // 4. Delete grade
        gradeRepository.delete(grade);
        log.info("Deleted grade: {}", gradeName);
    }

    @Override
    public List<GradeWithSectionsResponse> getAllGradesWithSections() {
        List<Grade> grades = gradeRepository.findAll();
        return grades.stream().map(grade -> {
            List<SimpleSectionResponse> sections = grade.getSections().stream()
                    .map(section -> new SimpleSectionResponse(section.getId(), section.getName()))
                    .toList();
            return new GradeWithSectionsResponse(grade.getId(), grade.getName(), sections);
        }).toList();
    }

    @Override
    public GradeStatsResponse getGradeStats(Long gradeId) {
        try {
            Grade grade = gradeRepository.findById(gradeId)
                    .orElseThrow(() -> new RuntimeException("Grade not found with ID: " + gradeId));

            long totalStudents = studentRepository.findByGradeId(gradeId).size();
            long totalTeachers = teacherRepository.findByGradeId(gradeId).size();

            log.info("Grade stats for {}: {} students, {} teachers",
                    grade.getName(), totalStudents, totalTeachers);

            return new GradeStatsResponse(grade.getName(), totalStudents, totalTeachers);

        } catch (Exception e) {
            log.error("Failed to fetch grade stats for ID {}: {}", gradeId, e.getMessage());
            throw new RuntimeException("Unable to fetch grade stats. Please try again later.");
        }
    }
}
