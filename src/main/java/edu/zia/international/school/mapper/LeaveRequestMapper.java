package edu.zia.international.school.mapper;

import edu.zia.international.school.dto.leave.CreateLeaveRequest;
import edu.zia.international.school.dto.leave.LeaveRequestResponse;
import edu.zia.international.school.entity.LeaveRequest;
import edu.zia.international.school.enums.LeaveStatus;
import org.springframework.stereotype.Component;

@Component
public class LeaveRequestMapper {

    public LeaveRequest toEntity(CreateLeaveRequest dto) {
        LeaveRequest entity = new LeaveRequest();
        entity.setEmpId(dto.empId());
        entity.setLeaveType(dto.leaveType());
        entity.setStartDate(dto.startDate());
        entity.setEndDate(dto.endDate());
        entity.setReason(dto.reason());
        entity.setStatus(LeaveStatus.PENDING); // default status
        return entity;
    }

    public LeaveRequestResponse toResponse(LeaveRequest entity) {
        return new LeaveRequestResponse(
                entity.getId(),
                entity.getEmpId(),
                entity.getEmpName(),
                entity.getLeaveType(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getReason(),
                entity.getStatus(),
                entity.getAdminRemarks(),
                entity.getAppliedOn()
        );
    }
}
