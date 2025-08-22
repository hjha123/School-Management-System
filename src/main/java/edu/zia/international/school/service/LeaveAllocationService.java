package edu.zia.international.school.service;


import edu.zia.international.school.dto.leave.LeaveAllocationRequest;
import edu.zia.international.school.dto.leave.LeaveAllocationResponse;

import java.util.List;

public interface LeaveAllocationService {
    List<LeaveAllocationResponse> allocateLeave(LeaveAllocationRequest request);

}
