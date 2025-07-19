package edu.zia.international.school.service.impl;

import edu.zia.international.school.dto.section.SectionRequest;
import edu.zia.international.school.dto.section.SectionResponse;
import edu.zia.international.school.dto.section.SimpleSectionResponse;
import edu.zia.international.school.entity.Grade;
import edu.zia.international.school.entity.Section;
import edu.zia.international.school.exception.ResourceNotFoundException;
import edu.zia.international.school.repository.GradeRepository;
import edu.zia.international.school.repository.SectionRepository;
import edu.zia.international.school.service.SectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SectionServiceImpl implements SectionService {

    private final SectionRepository sectionRepository;
    private final GradeRepository gradeRepository;

    @Override
    public SectionResponse createSection(SectionRequest request) {
        Grade grade = gradeRepository.findByName(request.getGradeName())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Grade not found with name: " + request.getGradeName()));

        Section section = Section.builder()
                .name(request.getName())
                .grade(grade)
                .build();

        sectionRepository.save(section);

        return new SectionResponse(
                section.getId(),
                section.getName(),
                grade.getId(),
                grade.getName()
        );
    }


    @Override
    public List<SectionResponse> getSectionsByGradeId(Long gradeId) {
        log.info("Fetching sections for grade ID: {}", gradeId);

        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found with ID: " + gradeId));

        return sectionRepository.findByGrade(grade)
                .stream()
                .map(section -> new SectionResponse(
                        section.getId(),
                        section.getName(),
                        grade.getId(),
                        grade.getName()))
                .collect(Collectors.toList());
    }


    @Override
    public void deleteSectionByGradeAndName(String gradeName, String sectionName) {
        Grade grade = gradeRepository.findByName(gradeName)
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found with name: " + gradeName));

        Section section = sectionRepository.findByNameAndGrade(sectionName, grade)
                .orElseThrow(() -> new ResourceNotFoundException("Section '" + sectionName + "' not found in Grade '" + gradeName + "'"));

        sectionRepository.delete(section);
        log.info("Deleted section '{}' from grade '{}'", sectionName, gradeName);
    }

    @Override
    public List<SimpleSectionResponse> getSimpleSectionsByGradeName(String gradeName) {
        log.info("Fetching simplified section list for grade: {}", gradeName);

        Grade grade = gradeRepository.findByNameIgnoreCase(gradeName)
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found with name: " + gradeName));

        // Use the sections list from Grade entity
        return grade.getSections().stream()
                .map(section -> new SimpleSectionResponse(section.getId(), section.getName()))
                .collect(Collectors.toList());
    }

}
