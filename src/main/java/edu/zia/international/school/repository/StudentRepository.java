package edu.zia.international.school.repository;

import edu.zia.international.school.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByStudentId(String studentId);
    Optional<Student> findByUsername(String username);
    boolean existsByEmail(String email);
    List<Student> findByGradeNameAndSectionName(String gradeName, String sectionName);
    List<Student> findByGradeName(String gradeName);
}

