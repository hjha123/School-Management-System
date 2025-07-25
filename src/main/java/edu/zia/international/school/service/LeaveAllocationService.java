package edu.zia.international.school.service;


import edu.zia.international.school.dto.BulkLeaveAllocationRequest;
import edu.zia.international.school.dto.leave.CreateLeaveAllocationRequest;
import edu.zia.international.school.dto.leave.LeaveAllocationResponse;

import java.util.List;

public interface LeaveAllocationService {
    LeaveAllocationResponse allocateLeave(CreateLeaveAllocationRequest request);
    List<LeaveAllocationResponse> allocateLeavesToMultipleEmployees(BulkLeaveAllocationRequest request);
}
