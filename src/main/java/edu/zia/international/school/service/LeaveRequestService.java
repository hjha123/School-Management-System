package edu.zia.international.school.service;


import edu.zia.international.school.dto.leave.*;

import java.util.List;

public interface LeaveRequestService {
    LeaveRequestResponse applyForLeave(CreateLeaveRequest request);
    LeaveRequestResponse updateLeaveStatus(Long leaveId, UpdateLeaveStatusRequest request);
    List<LeaveRequestResponse> getLeaveRequestsByEmpId(String empId);
    List<LeaveRequestResponse> getAllPendingLeaveRequests();
    List<LeaveRequestResponse> getAllLeaveRequests();
    LeaveBalanceResponse getLeaveBalanceByEmpId(String empId);

    List<LeaveRequestResponse> getMyLeaveRequests(String username);

    List<LeaveEntitlementResponse> getMyLeaveEntitlements();
}
