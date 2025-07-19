package edu.zia.international.school.service;

import edu.zia.international.school.dto.section.SectionRequest;
import edu.zia.international.school.dto.section.SectionResponse;
import edu.zia.international.school.dto.section.SimpleSectionResponse;

import java.util.List;

public interface SectionService {
    SectionResponse createSection(SectionRequest request);
    List<SectionResponse> getSectionsByGradeId(Long gradeId);
    void deleteSectionByGradeAndName(String gradeName, String sectionName);
    List<SimpleSectionResponse> getSimpleSectionsByGradeName(String gradeName);

}
