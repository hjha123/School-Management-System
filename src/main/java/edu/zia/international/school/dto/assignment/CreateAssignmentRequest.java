package edu.zia.international.school.dto.assignment;

import edu.zia.international.school.enums.AssignmentStatus;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAssignmentRequest {
    private String title;
    private String description;
    private LocalDate dueDate;
    private long gradeId;
    private long sectionId;
    private AssignmentStatus status;
    private String teacherId;
}
