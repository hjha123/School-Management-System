package edu.zia.international.school.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "student_serial")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentSerial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int year;

    private int lastSerial;
}
