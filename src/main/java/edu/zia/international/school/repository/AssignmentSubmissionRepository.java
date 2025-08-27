package edu.zia.international.school.repository;

import edu.zia.international.school.entity.AssignmentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {
    List<AssignmentSubmission> findByAssignmentId(Long assignmentId);
    AssignmentSubmission findByAssignmentIdAndStudentId(Long assignmentId, String studentId);
}
