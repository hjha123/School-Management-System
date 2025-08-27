package edu.zia.international.school.dto.assignment;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentSubmissionResponse {
    private Long id;
    private String studentId;
    private LocalDateTime submittedAt;
    private String fileUrl;
    private String textAnswer;
    private Double marks;
    private String feedback;
    private String status;
}
