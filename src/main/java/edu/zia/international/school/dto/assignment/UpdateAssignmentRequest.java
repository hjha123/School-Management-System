package edu.zia.international.school.dto.assignment;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class UpdateAssignmentRequest {
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private Long gradeId;
    private Long sectionId;
}

