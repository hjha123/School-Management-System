package edu.zia.international.school.repository;

import edu.zia.international.school.entity.Grade;
import edu.zia.international.school.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SectionRepository extends JpaRepository<Section, Long> {
    List<Section> findByGradeId(Long gradeId);
    Optional<Section> findByNameAndGradeId(String name, Long gradeId);
    List<Section> findByGrade(Grade grade);
    Optional<Section> findByNameAndGrade(String name, Grade grade);
    List<Section> findByGradeAndNameIn(Grade grade, List<String> names);
    Optional<Section> findByGradeAndNameIgnoreCase(Grade grade, String name);
    Optional<Section> findByName(String name);


}
