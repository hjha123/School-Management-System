package edu.zia.international.school.dto.leave;

import edu.zia.international.school.enums.LeaveType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record EmployeeLeaveAllocationRequest(
        @NotBlank String empId,
        @NotEmpty List<LeaveAllocationEntry> allocations
) {
    public record LeaveAllocationEntry(
            @NotNull LeaveType leaveType,
            @Min(0) int days
    ) {}
}
