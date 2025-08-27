package edu.zia.international.school.controller;

import edu.zia.international.school.dto.grade.GradeRequest;
import edu.zia.international.school.dto.grade.GradeResponse;
import edu.zia.international.school.dto.grade.GradeWithSectionsResponse;
import edu.zia.international.school.service.GradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
@Slf4j
public class GradeController {

    private final GradeService gradeService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<GradeResponse> createGrade(@Valid @RequestBody GradeRequest request) {
        log.info("Creating new grade: {}", request.getName());
        return new ResponseEntity<>(gradeService.createGrade(request), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @GetMapping("/{id}")
    public ResponseEntity<GradeResponse> getGradeById(@PathVariable Long id) {
        log.info("Fetching grade by ID: {}", id);
        return ResponseEntity.ok(gradeService.getGradeById(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @GetMapping
    public ResponseEntity<List<GradeResponse>> getAllGrades() {
        log.info("Fetching all grades");
        return ResponseEntity.ok(gradeService.getAllGrades());
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @DeleteMapping("/{gradeName}")
    public ResponseEntity<Void> deleteGradeByName(@PathVariable String gradeName) {
        log.info("Deleting grade and all related sections for grade: {}", gradeName);
        gradeService.deleteGradeByName(gradeName);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @GetMapping("/with-sections")
    public ResponseEntity<List<GradeWithSectionsResponse>> getAllGradesWithSections() {
        log.info("Fetching all grades with their sections");
        return ResponseEntity.ok(gradeService.getAllGradesWithSections());
    }


}
