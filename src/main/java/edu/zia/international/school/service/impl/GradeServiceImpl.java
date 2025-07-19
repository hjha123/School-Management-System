package edu.zia.international.school.service.impl;

import edu.zia.international.school.dto.grade.GradeRequest;
import edu.zia.international.school.dto.grade.GradeResponse;
import edu.zia.international.school.dto.grade.GradeWithSectionsResponse;
import edu.zia.international.school.dto.section.SimpleSectionResponse;
import edu.zia.international.school.entity.Grade;
import edu.zia.international.school.entity.Section;
import edu.zia.international.school.entity.Teacher;
import edu.zia.international.school.exception.ResourceNotFoundException;
import edu.zia.international.school.repository.GradeRepository;
import edu.zia.international.school.repository.SectionRepository;
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

        log.info("Disassociating teachers from sections and grade: {}", gradeName);

        // Disassociate teachers from sections and grade
        for (Section section : grade.getSections()) {
            for (Teacher teacher : section.getTeachers()) {
                teacher.setSection(null); // Remove section
                teacher.setGrade(null);   // ✅ Also remove grade
                teacherRepository.save(teacher);
            }

            log.info("Deleting section: {}", section.getName());
            sectionRepository.delete(section);
        }

        // Also disassociate teachers directly assigned to this grade (if not already handled above)
        List<Teacher> directTeachers = teacherRepository.findByGrade(grade);
        for (Teacher teacher : directTeachers) {
            teacher.setGrade(null);
            teacherRepository.save(teacher);
        }

        log.info("Deleting grade: {}", gradeName);
        gradeRepository.delete(grade);
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


}
