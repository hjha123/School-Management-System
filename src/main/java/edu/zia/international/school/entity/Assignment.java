package edu.zia.international.school.entity;

import edu.zia.international.school.enums.AssignmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDate dueDate;

    // Assigned to Grade & Section
    private long gradeId;
    private long sectionId;
    private String gradeName;
    private String sectionName;

    // Audit info
    private String createdByRole;
    private String createdByUserId;
    private String assignedTeacherId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String lastUpdatedBy;

    // Attachments
    @ElementCollection
    @CollectionTable(name = "assignment_attachments", joinColumns = @JoinColumn(name = "assignment_id"))
    @Column(name = "file_url")
    private List<String> attachments;

    // Status
    @Enumerated(EnumType.STRING)
    private AssignmentStatus status;

    private String adminRemarks;

    // Submissions by students
    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AssignmentSubmission> submissions;
}
