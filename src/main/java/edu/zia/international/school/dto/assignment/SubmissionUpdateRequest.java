package edu.zia.international.school.dto.assignment;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionUpdateRequest {
    private Double marks;
    private String feedback;
}
