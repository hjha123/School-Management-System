package edu.zia.international.school.dto.assignment;

import edu.zia.international.school.enums.SubmissionStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionUpdateRequest {
    private SubmissionStatus submissionStatus;
    private Double marks;
    private String feedback;
}
