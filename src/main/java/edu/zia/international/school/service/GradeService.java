package edu.zia.international.school.service;


import edu.zia.international.school.dto.grade.GradeRequest;
import edu.zia.international.school.dto.grade.GradeResponse;
import edu.zia.international.school.dto.grade.GradeWithSectionsResponse;

import java.util.List;

public interface GradeService {
    GradeResponse createGrade(GradeRequest request);
    List<GradeResponse> getAllGrades();
    GradeResponse getGradeById(Long id);
    void deleteGradeByName(String gradeName);
    List<GradeWithSectionsResponse> getAllGradesWithSections();

}
