package edu.zia.international.school.entity;

import edu.zia.international.school.enums.LeaveStatus;
import edu.zia.international.school.enums.LeaveType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String empId;

    @Column(nullable = false)
    private String empName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveType leaveType; // e.g. SICK, CASUAL, etc.

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    private String reason;

    @Enumerated(EnumType.STRING)
    private LeaveStatus status;

    private String adminRemarks;
    @Column(name = "applied_on", nullable = false, updatable = false)
    private LocalDate appliedOn;

}