package edu.zia.international.school.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "teacher_serial")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeacherSerial {
    @Id
    private int year;
    @Column(name = "last_serial", nullable = false)
    private int lastSerial;
}
