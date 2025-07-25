package edu.zia.international.school.dto.leave;

import edu.zia.international.school.enums.LeaveStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateLeaveStatusRequest(
        @NotNull LeaveStatus status,
        String adminRemarks
) {}
