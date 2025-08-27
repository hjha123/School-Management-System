package edu.zia.international.school.repository;

import edu.zia.international.school.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByCreatedByTeacherId(String teacherId);
    List<Assignment> findByGradeIdAndSectionId(Long gradeId, Long sectionId);
}
