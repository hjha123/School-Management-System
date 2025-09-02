package edu.zia.international.school.dto.assignment;

import edu.zia.international.school.enums.SubmissionStatus;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentSubmissionResponse {
    private Long id;
    private Long assignmentId;
    private String studentId;
    private String fileUrl;
    private String textAnswer;
    private Double marks;
    private String feedback;
    private SubmissionStatus submissionStatus;
    private LocalDateTime submittedAt;
}
