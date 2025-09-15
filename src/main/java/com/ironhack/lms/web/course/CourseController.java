package com.ironhack.lms.web.course;

import com.ironhack.lms.service.course.CourseService;
import com.ironhack.lms.web.course.dto.*;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService service;

    // ---- Public reads ----
    @PermitAll
    @GetMapping
    public Page<CourseResponse> listPublished(Pageable pageable) {
        return service.listPublished(pageable);
    }

    @PermitAll
    @GetMapping("/{id}")
    public CourseResponse get(@PathVariable Long id, Authentication auth) {
        return service.getForRead(id, auth);
    }

    // ---- Writes (instructor/admin) ----
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping
    public CourseResponse create(@Valid @RequestBody CourseCreateRequest req, Authentication auth) {
        return service.createCourse(req, auth);
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
    @PutMapping("/{id}")
    public CourseResponse update(@PathVariable Long id,
                                 @Valid @RequestBody CourseUpdateRequest req,
                                 Authentication auth) {
        return service.updateCourse(id, req, auth);
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        service.deleteCourse(id, auth);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
    @PostMapping("/{id}/lessons")
    public ResponseEntity<Long> addLesson(@PathVariable Long id,
                                          @Valid @RequestBody LessonCreateRequest req,
                                          Authentication auth) {
        return ResponseEntity.ok(service.addLesson(id, req, auth));
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
    @PostMapping("/{id}/assignments")
    public ResponseEntity<Long> addAssignment(@PathVariable Long id,
                                              @Valid @RequestBody AssignmentCreateRequest req,
                                              Authentication auth) {
        return ResponseEntity.ok(service.addAssignment(id, req, auth));
    }
}
