package edu.zia.international.school.dto.leave;

import java.util.Map;

public record LeaveBalanceResponse(
        String empId,
        String empName,
        int year,
        Map<String, LeaveTypeBalance> leaveBalances
) {}
