package edu.zia.international.school.repository;

import edu.zia.international.school.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GradeRepository extends JpaRepository<Grade, Long> {
    Optional<Grade> findByName(String name);
    Optional<Grade> findByNameIgnoreCase(String gradeName);
}
