package edu.zia.international.school.dto.grade;

import edu.zia.international.school.dto.section.SimpleSectionResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GradeWithSectionsResponse {
    private Long gradeId;
    private String gradeName;
    private List<SimpleSectionResponse> sections;
}
