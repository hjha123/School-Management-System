package edu.zia.international.school.dto.leave;

import edu.zia.international.school.enums.LeaveType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LeaveEntitlementResponse {
    private LeaveType leaveType;
    private int totalAllocated;
    private int usedLeaves;
    private int remainingLeaves;
}
