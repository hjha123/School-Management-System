package edu.zia.international.school.dto.grade;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GradeStatsResponse {
    private String gradeName;
    private long totalStudents;
    private long totalTeachers;
}
