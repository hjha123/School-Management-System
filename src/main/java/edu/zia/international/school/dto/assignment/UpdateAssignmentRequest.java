package edu.zia.international.school.dto.assignment;

import edu.zia.international.school.enums.AssignmentStatus;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class UpdateAssignmentRequest {
    private String title;
    private String description;
    private LocalDate dueDate;
    private Long gradeId;
    private Long sectionId;
    private AssignmentStatus status;
}

