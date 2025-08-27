package edu.zia.international.school.dto.assignment;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAssignmentRequest {
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private long gradeId;
    private long sectionId;
}
