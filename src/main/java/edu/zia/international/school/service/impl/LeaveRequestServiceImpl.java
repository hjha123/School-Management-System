package edu.zia.international.school.service.impl;

import edu.zia.international.school.dto.leave.*;
import edu.zia.international.school.entity.LeaveAllocation;
import edu.zia.international.school.entity.LeaveRequest;
import edu.zia.international.school.entity.Teacher;
import edu.zia.international.school.enums.LeaveStatus;
import edu.zia.international.school.exception.InvalidRequestException;
import edu.zia.international.school.exception.ResourceNotFoundException;
import edu.zia.international.school.mapper.LeaveRequestMapper;
import edu.zia.international.school.repository.LeaveAllocationRepository;
import edu.zia.international.school.repository.LeaveRequestRepository;
import edu.zia.international.school.repository.TeacherRepository;
import edu.zia.international.school.service.LeaveRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaveRequestServiceImpl implements LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveAllocationRepository leaveAllocationRepository;
    private final LeaveRequestMapper leaveRequestMapper;

    private final TeacherRepository teacherRepository;

    @Override
    @Transactional
    public LeaveRequestResponse applyForLeave(CreateLeaveRequest request) {
        log.info("Applying leave for empId: {} from {} to {}", request.empId(), request.startDate(), request.endDate());

        // 1. Check if employee exists
        Teacher teacher = teacherRepository.findByEmpId(request.empId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee with empId " + request.empId() + " not found"));

        LeaveAllocation allocation = leaveAllocationRepository.findByEmpIdAndLeaveTypeAndYear(
                        request.empId(), request.leaveType(), request.startDate().getYear())
                .orElseThrow(() -> new ResourceNotFoundException("Leave allocation not found for employee: " + request.empId()));

        long daysRequested = request.endDate().toEpochDay() - request.startDate().toEpochDay() + 1;

        if (allocation.getRemainingLeaves() < daysRequested) {
            throw new InvalidRequestException("Insufficient leave balance. Requested: " + daysRequested + ", Remaining: " + allocation.getRemainingLeaves());
        }

        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setEmpId(request.empId());
        leaveRequest.setEmpName(teacher.getFullName());
        leaveRequest.setLeaveType(request.leaveType());
        leaveRequest.setStartDate(request.startDate());
        leaveRequest.setEndDate(request.endDate());
        leaveRequest.setReason(request.reason());
        leaveRequest.setStatus(LeaveStatus.PENDING);
        leaveRequest.setAppliedOn(LocalDate.now());

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        log.info("Leave request submitted successfully for empId: {} with ID: {}", request.empId(), saved.getId());

        return leaveRequestMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public LeaveRequestResponse updateLeaveStatus(Long leaveId, UpdateLeaveStatusRequest request) {
        log.info("Updating leave request ID: {} to status: {}", leaveId, request.status());

        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with ID: " + leaveId));

        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new InvalidRequestException("Leave request already processed");
        }

        leaveRequest.setStatus(request.status());
        leaveRequest.setAdminRemarks(request.adminRemarks());
        LeaveRequest updated = leaveRequestRepository.save(leaveRequest);

        // Deduct leave only if approved
        if (request.status() == LeaveStatus.APPROVED) {
            LeaveAllocation allocation = leaveAllocationRepository.findByEmpIdAndLeaveTypeAndYear(
                            leaveRequest.getEmpId(),
                            leaveRequest.getLeaveType(),
                            leaveRequest.getStartDate().getYear())
                    .orElseThrow(() -> new ResourceNotFoundException("Leave allocation not found for empId: " + leaveRequest.getEmpId()));

            long days = ChronoUnit.DAYS.between(leaveRequest.getStartDate(), leaveRequest.getEndDate()) + 1;
            int updatedRemaining = allocation.getRemainingLeaves() - (int) days;
            allocation.setRemainingLeaves(updatedRemaining);
            leaveAllocationRepository.save(allocation);

            log.info("Leave balance updated for empId: {}. Remaining: {}", leaveRequest.getEmpId(), updatedRemaining);
        }

        return leaveRequestMapper.toResponse(updated);
    }


    @Override
    public List<LeaveRequestResponse> getLeaveRequestsByEmpId(String empId) {
        log.info("Fetching leave requests for empId: {}", empId);
        return leaveRequestRepository.findByEmpId(empId)
                .stream()
                .map(leaveRequestMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<LeaveRequestResponse> getAllLeaveRequests() {
        List<LeaveRequest> all = leaveRequestRepository.findAll();
        log.info("Retrieved [{}] total leave requests", all.size());
        return all.stream().map(leaveRequestMapper::toResponse).toList();
    }

    @Override
    public List<LeaveRequestResponse> getAllPendingLeaveRequests() {
        log.info("Fetching all pending leave requests");
        return leaveRequestRepository.findByStatus(LeaveStatus.PENDING)
                .stream()
                .map(leaveRequestMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public LeaveBalanceResponse getLeaveBalanceByEmpId(String empId) {
        log.info("Getting leave balance for empId: {}", empId);

        var teacher = teacherRepository.findByEmpId(empId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher with empId " + empId + " not found."));

        int currentYear = LocalDate.now().getYear();

        List<LeaveAllocation> allocations = leaveAllocationRepository.findByEmpIdAndYear(empId, currentYear);

        if (allocations.isEmpty()) {
            log.warn("No leave allocations found for empId={} in year={}", empId, currentYear);
        }

        // Create nested map
        Map<String, LeaveTypeBalance> leaveBalances = allocations.stream()
                .collect(Collectors.toMap(
                        alloc -> alloc.getLeaveType().name(),
                        alloc -> new LeaveTypeBalance(alloc.getTotalAllocatedLeaves(), alloc.getRemainingLeaves())
                ));

        return new LeaveBalanceResponse(
                empId,
                teacher.getFullName(),
                currentYear,
                leaveBalances
        );
    }

    @Override
    public List<LeaveRequestResponse> getMyLeaveRequests(String username) {
        log.info("Fetching leave requests for teacher: {}", username);

        Teacher teacher = teacherRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found for username: " + username));

        List<LeaveRequest> requests = leaveRequestRepository.findByEmpId(teacher.getEmpId());

        return requests.stream()
                .map(leaveRequestMapper::toResponse)
                .toList();
    }

    @Override
    public List<LeaveEntitlementResponse> getMyLeaveEntitlements() {
        log.info("Fetching leave entitlements for current teacher");

        // Get currently logged-in username (empId)
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Logged-in username: {}", username);

        Teacher teacher = teacherRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("Teacher not found for username: {}", username);
                    return new RuntimeException("Teacher not found");
                });

        log.info("Teacher found: {} ({})", teacher.getFullName(), teacher.getEmpId());

        int currentYear = Year.now().getValue();
        log.info("Fetching leave allocations for year: {}", currentYear);

        List<LeaveAllocation> allocations = leaveAllocationRepository
                .findByEmpIdAndYear(teacher.getEmpId(), currentYear);

        log.info("Found {} leave allocations for teacher {}", allocations.size(), teacher.getEmpId());

        List<LeaveEntitlementResponse> responses = allocations.stream()
                .map(a -> {
                    int usedLeaves = a.getTotalAllocatedLeaves() - a.getRemainingLeaves();
                    log.debug("LeaveType: {}, Total: {}, Used: {}, Remaining: {}",
                            a.getLeaveType(), a.getTotalAllocatedLeaves(), usedLeaves, a.getRemainingLeaves());

                    return LeaveEntitlementResponse.builder()
                            .leaveType(a.getLeaveType())
                            .totalAllocated(a.getTotalAllocatedLeaves())
                            .usedLeaves(usedLeaves)
                            .remainingLeaves(a.getRemainingLeaves())
                            .build();
                })
                .collect(Collectors.toList());

        log.info("Returning {} leave entitlement records", responses.size());
        return responses;
    }


}
