package edu.zia.international.school.dto.section;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SectionRequest {
    @NotBlank(message = "Section name is required")
    private String name;

    @NotBlank(message = "Grade name is required")
    private String gradeName;
}
