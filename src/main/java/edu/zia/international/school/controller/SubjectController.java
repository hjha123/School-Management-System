package edu.zia.international.school.controller;

import edu.zia.international.school.entity.Subject;
import edu.zia.international.school.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
@Slf4j
public class SubjectController {

    private final SubjectRepository subjectRepository;

    // 🔍 Get all subjects
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<Subject>> getAllSubjects() {
        log.info("Fetching all subjects");
        List<Subject> subjects = subjectRepository.findAll();
        return ResponseEntity.ok(subjects);
    }

    // 🔍 Get subject by ID
    @GetMapping("/{id}")
    public ResponseEntity<Subject> getSubjectById(@PathVariable Long id) {
        log.info("Fetching subject with ID: {}", id);
        return subjectRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ➕ Create subject
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Subject> createSubject(@RequestBody Subject subject) {
        log.info("Creating subject: {}", subject.getName());
        Subject saved = subjectRepository.save(subject);
        return ResponseEntity.ok(saved);
    }

    // ✏️ Update subject
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Subject> updateSubject(@PathVariable Long id, @RequestBody Subject updatedSubject) {
        log.info("Updating subject with ID: {}", id);
        return subjectRepository.findById(id)
                .map(existing -> {
                    existing.setName(updatedSubject.getName());
                    subjectRepository.save(existing);
                    return ResponseEntity.ok(existing);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ❌ Delete subject
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSubject(@PathVariable Long id) {
        log.info("Deleting subject with ID: {}", id);
        if (subjectRepository.existsById(id)) {
            subjectRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
