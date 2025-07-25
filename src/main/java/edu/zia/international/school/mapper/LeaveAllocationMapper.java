package edu.zia.international.school.mapper;

import edu.zia.international.school.dto.leave.LeaveAllocationResponse;
import edu.zia.international.school.entity.LeaveAllocation;

public class LeaveAllocationMapper {

    public static LeaveAllocationResponse toResponse(LeaveAllocation allocation) {
        return new LeaveAllocationResponse(
                allocation.getId(),
                allocation.getEmpId(),
                allocation.getLeaveType(),
                allocation.getYear(),
                allocation.getTotalAllocatedLeaves(),
                allocation.getRemainingLeaves()
        );
    }
}
