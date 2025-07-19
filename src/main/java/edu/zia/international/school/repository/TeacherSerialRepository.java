package edu.zia.international.school.repository;

import edu.zia.international.school.entity.TeacherSerial;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface TeacherSerialRepository extends JpaRepository<TeacherSerial, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<TeacherSerial> findByYear(int year);
}
