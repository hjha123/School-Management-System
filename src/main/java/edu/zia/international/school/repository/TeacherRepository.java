package edu.zia.international.school.repository;

import edu.zia.international.school.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}

