package edu.zia.international.school.controller;

import edu.zia.international.school.dto.leave.LeaveAllocationRequest;
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

    @PostMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LeaveAllocationResponse>> allocateLeave(
            @Valid @RequestBody LeaveAllocationRequest request
    ) {
        log.info("API called to allocate Leave for Employees: {}", request);
        List<LeaveAllocationResponse> responses = leaveAllocationService.allocateLeave(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

}
