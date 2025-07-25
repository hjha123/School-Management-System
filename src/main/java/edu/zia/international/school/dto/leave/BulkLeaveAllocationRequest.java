package edu.zia.international.school.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import edu.zia.international.school.enums.LeaveType;

public record BulkLeaveAllocationRequest(
        @NotNull List<String> empIds,  // All employee IDs
        @NotNull LeaveType leaveType,  // One leave type per request
        @Min(1) int year,
        @Min(1) int totalAllocated
) {}
