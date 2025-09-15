package com.ironhack.lms.web.enrollment;

import com.ironhack.lms.service.enrollment.EnrollmentService;
import com.ironhack.lms.web.enrollment.dto.EnrollmentResponse;
import jakarta.annotation.security.RolesAllowed;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService service;

    // Enroll in a published course
    @RolesAllowed("STUDENT")
    @PostMapping("/api/courses/{courseId}/enroll")
    public ResponseEntity<EnrollmentResponse> enroll(@PathVariable Long courseId, Authentication auth) {
        return ResponseEntity.ok(service.enroll(courseId, auth));
    }

    // My enrollments
    @RolesAllowed("STUDENT")
    @GetMapping("/api/enrollments")
    public Page<EnrollmentResponse> myEnrollments(Authentication auth, Pageable pageable) {
        return service.myEnrollments(auth, pageable);
    }

    // Cancel my enrollment
    @RolesAllowed("STUDENT")
    @PatchMapping("/api/enrollments/{id}/cancel")
    public EnrollmentResponse cancel(@PathVariable Long id, Authentication auth) {
        return service.cancel(id, auth);
    }

    // Mark as completed
    @RolesAllowed({ "INSTRUCTOR", "ADMIN" })
    @PatchMapping("/api/enrollments/{id}/complete")
    public EnrollmentResponse complete(@PathVariable Long id, Authentication auth) {
        return service.completeByStaff(id, auth);
    }
}
