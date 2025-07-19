package edu.zia.international.school.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "teachers")
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;

    private String email;

    private String username;

    private String phone;

    @Column(nullable = false)
    private String role = "TEACHER";

    private String gender;

    private LocalDate dateOfBirth;

    private String qualification;

    private String address;

    private LocalDate joiningDate;

    private Integer experienceYears;

    @ManyToMany
    @JoinTable(
            name = "teacher_subjects",
            joinColumns = @JoinColumn(name = "teacher_id"),
            inverseJoinColumns = @JoinColumn(name = "subject_id")
    )
    private List<Subject> subjects;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}
