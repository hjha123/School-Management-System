package edu.zia.international.school.entity;

import edu.zia.international.school.enums.TeacherStatus;
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

    // Basic Info
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(length = 10, unique = true)
    private String phone;

    @Column(nullable = false)
    private String role = "TEACHER";

    private String gender;

    private LocalDate dateOfBirth;

    private String qualification;

    @Column(length = 255)
    private String address;

    private LocalDate joiningDate;

    private Integer experienceYears;

    @Column(unique = true)
    private String empId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TeacherStatus status = TeacherStatus.ACTIVE;

    // Optional Additional Fields
    private String maritalStatus;

    private String emergencyContactInfo;

    private String bloodGroup;

    private String nationality;

    @Column(unique = true)
    private String aadharNumber;

    private String profileImageUrl;

    private String teacherType;  // FULL_TIME, PART_TIME, GUEST, VISITING

    // Subjects
    @ManyToMany
    @JoinTable(
            name = "teacher_subjects",
            joinColumns = @JoinColumn(name = "teacher_id"),
            inverseJoinColumns = @JoinColumn(name = "subject_id")
    )
    private List<Subject> subjects;

    // Optional Grade assignment
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_id")
    private Grade grade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private Section section;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}
