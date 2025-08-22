package edu.zia.international.school.dto.leave;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LeaveTypeResponse {
    private String name;
    private String displayName;
}
