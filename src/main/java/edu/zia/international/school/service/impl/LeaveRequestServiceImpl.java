package edu.zia.international.school.service.impl;

import edu.zia.international.school.dto.leave.CreateLeaveRequest;
import edu.zia.international.school.dto.leave.LeaveRequestResponse;
import edu.zia.international.school.dto.leave.UpdateLeaveStatusRequest;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.List;
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
}
