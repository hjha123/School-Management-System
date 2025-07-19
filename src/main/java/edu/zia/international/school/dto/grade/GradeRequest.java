package edu.zia.international.school.dto.grade;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GradeRequest {
    @NotBlank(message = "Grade name is required")
    private String name;
}
