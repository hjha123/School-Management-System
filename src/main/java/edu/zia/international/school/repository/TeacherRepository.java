package edu.zia.international.school.repository;

import edu.zia.international.school.entity.Grade;
import edu.zia.international.school.entity.Section;
import edu.zia.international.school.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<Teacher> findByGrade(Grade grade);
    @Query("SELECT COUNT(t) FROM Teacher t WHERE YEAR(t.joiningDate) = :year")
    long countByJoiningYear(@Param("year") int year);
    Optional<Teacher> findByEmpId(String empId);
    Optional<Teacher> findByUsername(String username);
    List<Teacher> findBySection(Section section);
    @Query("SELECT t FROM Teacher t " +
            "WHERE (:gradeId IS NULL OR t.grade.id = :gradeId) " +
            "AND (:sectionId IS NULL OR t.section.id = :sectionId) " +
            "AND (:name IS NULL OR LOWER(t.fullName) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:empId IS NULL OR t.empId = :empId) " +
            "AND (:teacherType IS NULL OR LOWER(t.teacherType) = LOWER(:teacherType))")
    List<Teacher> searchTeachers(Long gradeId, Long sectionId, String name, String empId, String teacherType);

}

