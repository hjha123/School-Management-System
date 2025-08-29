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

    // 🔹 By gradeId + sectionId
    List<Teacher> findByGradeIdAndSectionId(Long gradeId, Long sectionId);

    // 🔹 By gradeId only
    List<Teacher> findByGradeId(Long gradeId);

    // 🔹 By sectionId only
    List<Teacher> findBySectionId(Long sectionId);

    // 🔹 By gradeName + sectionName using JPQL
    @Query("SELECT t FROM Teacher t WHERE t.grade.name = :gradeName AND t.section.name = :sectionName")
    List<Teacher> findByGradeNameAndSectionName(@Param("gradeName") String gradeName,
                                                @Param("sectionName") String sectionName);

    // 🔹 By gradeName only
    @Query("SELECT t FROM Teacher t WHERE t.grade.name = :gradeName")
    List<Teacher> findByGradeName(@Param("gradeName") String gradeName);

    // 🔹 By sectionName only
    @Query("SELECT t FROM Teacher t WHERE t.section.name = :sectionName")
    List<Teacher> findBySectionName(@Param("sectionName") String sectionName);


}

