package edu.zia.international.school.controller;

import edu.zia.international.school.dto.teacher.CreateTeacherRequest;
import edu.zia.international.school.dto.teacher.TeacherResponse;
import edu.zia.international.school.dto.teacher.UpdateTeacherRequest;
import edu.zia.international.school.service.TeacherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TeacherResponse> createTeacher(@Valid @RequestBody CreateTeacherRequest request) {
        log.info("Received request to create teacher: {}", request.getEmail());
        TeacherResponse created = teacherService.createTeacher(request);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TeacherResponse>> getAllTeachers() {
        log.info("Fetching all teachers");
        return ResponseEntity.ok(teacherService.getAllTeachers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TeacherResponse> getTeacherById(@PathVariable Long id) {
        log.info("Fetching teacher with ID: {}", id);
        return ResponseEntity.ok(teacherService.getTeacherById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TeacherResponse> updateTeacher(@PathVariable Long id, @RequestBody UpdateTeacherRequest request) {
        log.info("Received request to update teacher ID: {}", id);
        return ResponseEntity.ok(teacherService.updateTeacher(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteTeacher(@PathVariable Long id) {
        log.info("Deleting teacher with ID: {}", id);
        teacherService.deleteTeacher(id);
        return ResponseEntity.ok("Teacher deleted successfully with id: " + id);
    }
}


