package edu.zia.international.school.dto.leave;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import edu.zia.international.school.enums.LeaveType;

public record LeaveAllocationRequest(
        @NotNull List<String> empIds,
        @NotNull LeaveType leaveType,
        @Min(1) int year,
        @Min(1) int totalAllocated
) {}
