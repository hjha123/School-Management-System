package edu.zia.international.school.dto.leave;

import edu.zia.international.school.enums.LeaveType;

public record LeaveAllocationResponse(
        Long id,
        String empId,
        LeaveType leaveType,
        int year,
        int totalAllocated,
        int remaining
) {}
