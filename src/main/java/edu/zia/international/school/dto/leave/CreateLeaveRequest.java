package edu.zia.international.school.dto.leave;

import edu.zia.international.school.enums.LeaveType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateLeaveRequest(
        @NotBlank String empId,
        @NotNull LeaveType leaveType,
        @NotNull @Future LocalDate startDate,
        @NotNull @Future LocalDate endDate,
        @NotBlank String reason
) {}
