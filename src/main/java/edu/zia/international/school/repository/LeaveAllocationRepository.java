package edu.zia.international.school.repository;

import edu.zia.international.school.entity.LeaveAllocation;
import edu.zia.international.school.enums.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LeaveAllocationRepository extends JpaRepository<LeaveAllocation, Long> {
    Optional<LeaveAllocation> findByEmpIdAndLeaveTypeAndYear(String empId, LeaveType leaveType, int year);
    List<LeaveAllocation> findByEmpIdAndYear(String empId, int year);

}
