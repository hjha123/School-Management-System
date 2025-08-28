package edu.zia.international.school.dto.assignment;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentResponse {
    private Long id;
    private String title;
    private String description;
    private LocalDate dueDate;
    private String gradeName;
    private String sectionName;
    private String createdByTeacherId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> attachments;
    private String status;
}
