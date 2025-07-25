package edu.zia.international.school.dto.leave;

import edu.zia.international.school.enums.LeaveType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateLeaveAllocationRequest(
        @NotBlank String empId,
        @NotNull LeaveType leaveType,
        @Min(1) int year,
        @Min(1) int totalAllocated
) {}
