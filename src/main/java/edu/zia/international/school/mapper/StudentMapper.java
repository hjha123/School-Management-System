package edu.zia.international.school.mapper;

import edu.zia.international.school.dto.student.StudentResponse;
import edu.zia.international.school.entity.Student;

public class StudentMapper {
    public static StudentResponse toResponse(Student student) {
        return StudentResponse.builder()
                .id(student.getId())
                .studentId(student.getStudentId())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .email(student.getEmail())
                .phone(student.getPhone())
                .gender(student.getGender())
                .dateOfBirth(student.getDateOfBirth() != null ? student.getDateOfBirth().toString() : null)
                .gradeName(student.getGradeName())
                .sectionName(student.getSectionName())
                .address(student.getAddress())
                .emergencyContactName(student.getEmergencyContactName())
                .emergencyContactPhone(student.getEmergencyContactPhone())
                .bloodGroup(student.getBloodGroup())
                .nationality(student.getNationality())
                .profileImageUrl(student.getProfileImageUrl())
                .username(student.getUsername())
                .status(student.getStatus().name())
                .guardianName(student.getGuardianName())
                .guardianPhone(student.getGuardianPhone())
                .admissionDate(student.getAdmissionDate())
                .build();
    }
}
