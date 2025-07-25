package edu.zia.international.school.service.impl;

import edu.zia.international.school.dto.BulkLeaveAllocationRequest;
import edu.zia.international.school.dto.leave.CreateLeaveAllocationRequest;
import edu.zia.international.school.dto.leave.LeaveAllocationResponse;
import edu.zia.international.school.entity.LeaveAllocation;
import edu.zia.international.school.exception.ResourceAlreadyExistsException;
import edu.zia.international.school.exception.ResourceNotFoundException;
import edu.zia.international.school.mapper.LeaveAllocationMapper;
import edu.zia.international.school.repository.LeaveAllocationRepository;
import edu.zia.international.school.repository.TeacherRepository;
import edu.zia.international.school.service.LeaveAllocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveAllocationServiceImpl implements LeaveAllocationService {

    private final LeaveAllocationRepository leaveAllocationRepository;

    private final TeacherRepository teacherRepository;

    @Override
    public LeaveAllocationResponse allocateLeave(CreateLeaveAllocationRequest request) {
        log.info("Allocating leave for empId={} type={} year={}",
                request.empId(), request.leaveType(), request.year());

        teacherRepository.findByEmpId(request.empId())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher with empId " + request.empId() + " not found."));

        leaveAllocationRepository.findByEmpIdAndLeaveTypeAndYear(request.empId(), request.leaveType(), request.year())
                .ifPresent(existing -> {
                    throw new ResourceAlreadyExistsException("Leave already allocated for this employee, type, and year.");
                });

        LeaveAllocation entity = LeaveAllocation.builder()
                .empId(request.empId())
                .leaveType(request.leaveType())
                .year(request.year())
                .totalAllocatedLeaves(request.totalAllocated())
                .remainingLeaves(request.totalAllocated())
                .build();

        LeaveAllocation saved = leaveAllocationRepository.save(entity);

        log.info("Leave allocation saved with id={}", saved.getId());

        return new LeaveAllocationResponse(
                saved.getId(),
                saved.getEmpId(),
                saved.getLeaveType(),
                saved.getYear(),
                saved.getTotalAllocatedLeaves(),
                saved.getRemainingLeaves()
        );
    }

    @Override
    public List<LeaveAllocationResponse> allocateLeavesToMultipleEmployees(BulkLeaveAllocationRequest request) {
        log.info("Allocating bulk leave for {} employees for year={} and type={}",
                request.empIds().size(), request.year(), request.leaveType());

        List<LeaveAllocationResponse> responseList = new ArrayList<>();

        for (String empId : request.empIds()) {
            try {
                // Check if employee exists
                teacherRepository.findByEmpId(empId)
                        .orElseThrow(() -> new ResourceNotFoundException("Teacher with empId " + empId + " not found."));

                // Check for existing allocation
                leaveAllocationRepository.findByEmpIdAndLeaveTypeAndYear(empId, request.leaveType(), request.year())
                        .ifPresent(existing -> {
                            throw new ResourceAlreadyExistsException("Leave already allocated for empId=" + empId);
                        });

                // Create and save
                LeaveAllocation allocation = LeaveAllocation.builder()
                        .empId(empId)
                        .leaveType(request.leaveType())
                        .year(request.year())
                        .totalAllocatedLeaves(request.totalAllocated())
                        .remainingLeaves(request.totalAllocated())
                        .build();

                LeaveAllocation saved = leaveAllocationRepository.save(allocation);
                responseList.add(LeaveAllocationMapper.toResponse(saved));

                log.info("Allocated leave for empId={}", empId);

            } catch (ResourceNotFoundException | ResourceAlreadyExistsException ex) {
                log.warn("Skipping leave allocation for empId={}: {}", empId, ex.getMessage());
            } catch (Exception ex) {
                log.error("Unexpected error allocating leave for empId={}: {}", empId, ex.getMessage(), ex);
            }
        }

        log.info("Bulk leave allocation completed. Total successful allocations: {}", responseList.size());
        return responseList;
    }


}
