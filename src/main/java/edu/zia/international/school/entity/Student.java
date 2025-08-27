package edu.zia.international.school.entity;

import edu.zia.international.school.enums.StudentStatus;
import edu.zia.international.school.enums.TeacherStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String studentId; // Auto-generated

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String phone;

    @Column(nullable = false)
    private String gender;

    @Column(nullable = false)
    private String dateOfBirth; // yyyy-MM-dd

    @Column(nullable = false)
    private String gradeName;

    @Column(nullable = false)
    private String sectionName;

    private String address;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String bloodGroup;
    private String nationality;
    private String profileImageUrl;

    @Column(nullable = false, unique = true)
    private String username; // Auto-generated

    @Column(nullable = false)
    private String password; // Encrypted

    @Column(nullable = false)
    private String role = "STUDENT";

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StudentStatus status = StudentStatus.ACTIVE;

    @Column(name = "guardian_name")
    private String guardianName;

    @Column(name = "guardian_phone")
    private String guardianPhone;

    @Column(name = "admission_date")
    private LocalDate admissionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_id")
    private Grade grade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private Section section;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User user;


}
