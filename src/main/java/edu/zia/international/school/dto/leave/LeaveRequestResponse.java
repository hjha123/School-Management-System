package edu.zia.international.school.dto.leave;

import edu.zia.international.school.enums.LeaveStatus;
import edu.zia.international.school.enums.LeaveType;

import java.time.LocalDate;

public record LeaveRequestResponse(
        Long id,
        String empId,
        String empName,
        LeaveType leaveType,
        LocalDate startDate,
        LocalDate endDate,
        String reason,
        LeaveStatus status,
        String adminRemarks
) {}
