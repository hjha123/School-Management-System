package edu.zia.international.school.controller;

import edu.zia.international.school.dto.section.SectionRequest;
import edu.zia.international.school.dto.section.SectionResponse;
import edu.zia.international.school.dto.section.SimpleSectionResponse;
import edu.zia.international.school.service.SectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sections")
@RequiredArgsConstructor
@Slf4j
public class SectionController {

    private final SectionService sectionService;

    // ✅ Create a new Section under a Grade
    @PostMapping("createSection")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SectionResponse> createSection(@Valid @RequestBody SectionRequest request) {
        log.info("Creating section '{}' under Grade '{}'", request.getName(), request.getGradeName());
        SectionResponse response = sectionService.createSection(request);
        return ResponseEntity.ok(response);
    }


    // ✅ Get all sections for a specific grade
    @GetMapping("/grade/{gradeId}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<List<SectionResponse>> getSectionsByGrade(@PathVariable Long gradeId) {
        log.info("Fetching sections for Grade ID: {}", gradeId);
        return ResponseEntity.ok(sectionService.getSectionsByGradeId(gradeId));
    }


    // ✅ Delete section
    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSectionByGradeAndName(
            @RequestParam String gradeName,
            @RequestParam String sectionName) {

        log.info("Deleting section '{}' under Grade '{}'", sectionName, gradeName);
        sectionService.deleteSectionByGradeAndName(gradeName, sectionName);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/grade/{gradeName}/simple")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<List<SimpleSectionResponse>> getSimpleSectionsByGrade(@PathVariable String gradeName) {
        log.info("Fetching simple section list for Grade: {}", gradeName);
        List<SimpleSectionResponse> sections = sectionService.getSimpleSectionsByGradeName(gradeName);
        return ResponseEntity.ok(sections);
    }



}
