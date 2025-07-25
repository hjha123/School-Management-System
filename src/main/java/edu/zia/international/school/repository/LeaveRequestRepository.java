package edu.zia.international.school.repository;

import edu.zia.international.school.entity.LeaveRequest;
import edu.zia.international.school.enums.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByEmpId(String staffId);
    List<LeaveRequest> findByStatus(LeaveStatus status);
}
