package edu.zia.international.school.repository;

import edu.zia.international.school.entity.StudentSerial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface StudentSerialRepository extends JpaRepository<StudentSerial, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<StudentSerial> findByYear(int year);
}
