package edu.zia.international.school.controller;

import edu.zia.international.school.dto.leave.CreateLeaveRequest;
import edu.zia.international.school.dto.leave.LeaveRequestResponse;
import edu.zia.international.school.dto.leave.UpdateLeaveStatusRequest;
import edu.zia.international.school.service.LeaveRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
@Slf4j
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    /**
     * 🧑‍🏫 Teacher applies for leave.
     */
    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<LeaveRequestResponse> applyForLeave(@Valid @RequestBody CreateLeaveRequest request) {
        log.info("Teacher [{}] applying for leave of type [{}] from {} to {}",
                request.empId(), request.leaveType(), request.startDate(), request.endDate());

        LeaveRequestResponse response = leaveRequestService.applyForLeave(request);
        return ResponseEntity.ok(response);
    }

    /**
     * ✅ Admin updates leave status (approve or reject).
     */
    @PutMapping("/requests/{leaveId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LeaveRequestResponse> updateLeaveStatus(
            @PathVariable Long leaveId,
            @Valid @RequestBody UpdateLeaveStatusRequest request
    ) {
        log.info("Admin updating leave status for leaveId [{}] to [{}]", leaveId, request.status());
        LeaveRequestResponse response = leaveRequestService.updateLeaveStatus(leaveId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 🔍 Get all leave requests (Admin only).
     */
    @GetMapping("/requests")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LeaveRequestResponse>> getAllLeaveRequests() {
        log.info("Admin fetching all leave requests");
        List<LeaveRequestResponse> responses = leaveRequestService.getAllLeaveRequests();
        return ResponseEntity.ok(responses);
    }

    /**
     * 📄 Get leave requests by empId (Teacher or Admin).
     */
    @GetMapping("/employee/{empId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LeaveRequestResponse>> getLeaveRequestsForEmployee(@PathVariable String empId) {
        log.info("Admin fetching leave requests for empId: {}", empId);
        List<LeaveRequestResponse> responses = leaveRequestService.getLeaveRequestsByEmpId(empId);
        return ResponseEntity.ok(responses);
    }


    @GetMapping("/my-requests")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<LeaveRequestResponse>> getMyLeaveRequests(@PathVariable String empId/*@AuthenticationPrincipal Jwt principal*/) {
        log.info("Teacher fetching own leave requests for empId: {}", empId);
        List<LeaveRequestResponse> responses = leaveRequestService.getLeaveRequestsByEmpId(empId);
        return ResponseEntity.ok(responses);
    }

    /**
     * ⏳ Get only PENDING leave requests (Admin only).
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LeaveRequestResponse>> getPendingLeaveRequests() {
        log.info("Admin fetching PENDING leave requests");
        List<LeaveRequestResponse> responses = leaveRequestService.getAllPendingLeaveRequests();
        return ResponseEntity.ok(responses);
    }

    /**
     * 📚 Get full leave history for an employee (Admin only).
     */
    @GetMapping("/history/{empId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LeaveRequestResponse>> getLeaveHistoryByEmpId(@PathVariable String empId) {
        log.info("Admin fetching full leave history for empId: {}", empId);
        List<LeaveRequestResponse> responses = leaveRequestService.getLeaveRequestsByEmpId(empId);
        return ResponseEntity.ok(responses);
    }

}
