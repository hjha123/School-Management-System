package edu.zia.international.school.controller;

import edu.zia.international.school.dto.leave.BulkLeaveAllocationRequest;
import edu.zia.international.school.dto.leave.CreateLeaveAllocationRequest;
import edu.zia.international.school.dto.leave.EmployeeLeaveAllocationRequest;
import edu.zia.international.school.dto.leave.LeaveAllocationResponse;
import edu.zia.international.school.service.LeaveAllocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/allocate/leaves")
@RequiredArgsConstructor
public class LeaveAllocationController {

    private final LeaveAllocationService leaveAllocationService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public LeaveAllocationResponse allocateLeave(@Valid @RequestBody CreateLeaveAllocationRequest request) {
        log.info("API called to allocate leave: {}", request);
        return leaveAllocationService.allocateLeave(request);
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LeaveAllocationResponse>> allocateBulkLeaves(
            @Valid @RequestBody BulkLeaveAllocationRequest request
    ) {
        log.info("API called to allocate Leave in Bulk to Multiple Employees: {}", request);
        List<LeaveAllocationResponse> responses = leaveAllocationService.allocateLeavesToAllEmployees(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    @PostMapping("/bulk-allocate")
    @PreAuthorize("hasRole('ADMIN')")
    public List<LeaveAllocationResponse> allocateLeavesInBulkToAnEmployee(
            @Valid @RequestBody EmployeeLeaveAllocationRequest request) {
        log.info("API called to bulk allocate leaves to an employee: {}", request);
        return leaveAllocationService.allocateLeaveToAnEmployee(request);
    }

}
