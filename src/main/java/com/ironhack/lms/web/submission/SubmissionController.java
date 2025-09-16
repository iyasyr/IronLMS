package com.ironhack.lms.web.submission;

import com.ironhack.lms.service.submission.SubmissionService;
import com.ironhack.lms.web.submission.dto.*;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService service;

    // ----- Student -----
    @RolesAllowed("STUDENT")
    @PostMapping("/api/assignments/{assignmentId}/submissions")
    public ResponseEntity<SubmissionResponse> submit(@PathVariable Long assignmentId,
                                                     @Valid @RequestBody SubmissionCreateRequest req,
                                                     Authentication auth) {
        return ResponseEntity.ok(service.submit(assignmentId, req, auth));
    }

    @RolesAllowed("STUDENT")
    @GetMapping("/api/submissions/mine")
    public Page<SubmissionResponse> mySubmissions(Authentication auth, Pageable pageable) {
        return service.mySubmissions(auth, pageable);
    }

    // ----- Instructor/Admin -----
    @RolesAllowed({"INSTRUCTOR","ADMIN"})
    @GetMapping("/api/courses/{courseId}/submissions")
    public Page<SubmissionResponse> listByCourse(@PathVariable Long courseId,
                                                 Authentication auth, Pageable pageable) {
        return service.listByCourse(courseId, auth, pageable);
    }

    @RolesAllowed({"INSTRUCTOR","ADMIN"})
    @PatchMapping("/api/submissions/{id}/grade")
    public SubmissionResponse grade(@PathVariable Long id,
                                    @Valid @RequestBody GradeRequest req,
                                    Authentication auth) {
        return service.grade(id, req, auth);
    }

    @RolesAllowed({"INSTRUCTOR","ADMIN"})
    @PatchMapping("/api/submissions/{id}/request-resubmission")
    public SubmissionResponse requestResubmission(@PathVariable Long id,
                                                  @Valid @RequestBody ResubmitRequest req,
                                                  Authentication auth) {
        return service.requestResubmission(id, req, auth);
    }
}
