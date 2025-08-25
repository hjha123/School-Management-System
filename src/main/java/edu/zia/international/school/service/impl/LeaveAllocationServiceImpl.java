package edu.zia.international.school.service.impl;

import edu.zia.international.school.dto.leave.LeaveAllocationRequest;
import edu.zia.international.school.dto.leave.LeaveAllocationResponse;
import edu.zia.international.school.entity.LeaveAllocation;
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
    public List<LeaveAllocationResponse> allocateLeave(LeaveAllocationRequest request) {
        log.info("Allocating/Updating leave for {} employees for year={} and type={}",
                request.empIds().size(), request.year(), request.leaveType());

        List<LeaveAllocationResponse> responseList = new ArrayList<>();

        for (String empId : request.empIds()) {
            try {
                // Check if employee exists
                teacherRepository.findByEmpId(empId)
                        .orElseThrow(() -> new ResourceNotFoundException("Teacher with empId " + empId + " not found."));

                LeaveAllocation allocation = leaveAllocationRepository
                        .findByEmpIdAndLeaveTypeAndYear(empId, request.leaveType(), request.year())
                        .map(existing -> {
                            log.info("Updating existing leave allocation for empId={} year={} type={}",
                                    empId, request.year(), request.leaveType());

                            // Leaves already used from the previous allocation
                            int leavesUsed = existing.getTotalAllocatedLeaves() - existing.getRemainingLeaves();

                            // Add the new allocation to the old total
                            int updatedTotal = existing.getTotalAllocatedLeaves() + request.totalAllocated();
                            existing.setTotalAllocatedLeaves(updatedTotal);

                            // Recalculate remaining = updated total - leaves used
                            int newRemaining = updatedTotal - leavesUsed;
                            existing.setRemainingLeaves(Math.max(newRemaining, 0));

                            return existing;
                        })
                        .orElseGet(() -> {
                            log.info("Creating new leave allocation for empId={} year={} type={}", empId, request.year(), request.leaveType());
                            return LeaveAllocation.builder()
                                    .empId(empId)
                                    .leaveType(request.leaveType())
                                    .year(request.year())
                                    .totalAllocatedLeaves(request.totalAllocated())
                                    .remainingLeaves(request.totalAllocated())
                                    .build();
                        });


                LeaveAllocation saved = leaveAllocationRepository.save(allocation);
                responseList.add(LeaveAllocationMapper.toResponse(saved));

            } catch (ResourceNotFoundException ex) {
                log.warn("Skipping leave allocation for empId={}: {}", empId, ex.getMessage());
            } catch (Exception ex) {
                log.error("Unexpected error allocating leave for empId={}: {}", empId, ex.getMessage(), ex);
            }
        }

        log.info("Leave allocation/update completed. Total processed: {}", responseList.size());
        return responseList;
    }
    
}
