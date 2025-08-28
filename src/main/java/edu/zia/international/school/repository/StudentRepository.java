package edu.zia.international.school.repository;

import edu.zia.international.school.entity.Section;
import edu.zia.international.school.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByStudentId(String studentId);
    Optional<Student> findByUsername(String username);
    boolean existsByEmail(String email);
    List<Student> findByGradeNameAndSectionName(String gradeName, String sectionName);
    List<Student> findByGradeName(String gradeName);
    List<Student> findBySectionName(String gradeName);
    List<Student> findBySection(Section section);
    List<Student> findByGradeIdAndSectionId(Long gradeId, Long sectionId);
    List<Student> findByGradeId(Long gradeId);

    List<Student> findBySectionId(Long sectionId);

    @Query("SELECT s FROM Student s " +
            "WHERE (:studentId IS NULL OR s.studentId = :studentId) " +
            "AND (:name IS NULL OR LOWER(CONCAT(s.firstName, ' ', s.lastName)) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:gradeName IS NULL OR s.gradeName = :gradeName) " +
            "AND (:gradeId IS NULL OR s.grade.id = :gradeId) " +
            "AND (:sectionName IS NULL OR s.sectionName = :sectionName) " +
            "AND (:sectionId IS NULL OR s.section.id = :sectionId)")
    List<Student> searchStudents(@Param("studentId") String studentId,
                                 @Param("name") String name,
                                 @Param("gradeName") String gradeName,
                                 @Param("gradeId") Long gradeId,
                                 @Param("sectionName") String sectionName,
                                 @Param("sectionId") Long sectionId);


}

