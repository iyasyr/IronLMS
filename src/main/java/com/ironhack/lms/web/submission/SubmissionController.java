package com.ironhack.lms.web.submission;

import com.ironhack.lms.service.submission.SubmissionService;
import com.ironhack.lms.web.submission.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Submissions", description = "Assignment submission and grading endpoints for students and instructors")
@SecurityRequirement(name = "bearerAuth")
public class SubmissionController {

    private final SubmissionService service;

    @Operation(
            summary = "Submit Assignment",
            description = "Submit an assignment for grading. Only students enrolled in the course can submit assignments.",
            operationId = "submitAssignment"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Assignment submitted successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SubmissionResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Assignment not found"),
            @ApiResponse(responseCode = "403", description = "Access denied - Not enrolled in course"),
            @ApiResponse(responseCode = "400", description = "Invalid submission data")
    })
    @RolesAllowed("STUDENT")
    @PostMapping("/api/assignments/{assignmentId}/submissions")
    public ResponseEntity<SubmissionResponse> submit(
            @Parameter(description = "Assignment ID", required = true, example = "1")
            @PathVariable Long assignmentId,
            @Valid @RequestBody SubmissionCreateRequest req,
            Authentication auth) {
        return ResponseEntity.ok(service.submit(assignmentId, req, auth));
    }

    @Operation(
            summary = "Get My Submissions",
            description = "Retrieve a paginated list of the authenticated student's assignment submissions.",
            operationId = "getMySubmissions"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved submissions",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)
                    )
            ),
            @ApiResponse(responseCode = "403", description = "Access denied - Student role required")
    })
    @RolesAllowed("STUDENT")
    @GetMapping("/api/submissions/mine")
    public Page<SubmissionResponse> mySubmissions(
            Authentication auth, 
            @Parameter(description = "Pagination parameters (page, size, sort)")
            Pageable pageable) {
        return service.mySubmissions(auth, pageable);
    }

    @Operation(
            summary = "List Course Submissions",
            description = "Retrieve all submissions for assignments in a specific course. " +
                    "Only instructors and admins can view course submissions.",
            operationId = "listCourseSubmissions"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved course submissions",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Course not found"),
            @ApiResponse(responseCode = "403", description = "Access denied - Instructor/Admin role required")
    })
    @RolesAllowed({"INSTRUCTOR","ADMIN"})
    @GetMapping("/api/courses/{courseId}/submissions")
    public Page<SubmissionResponse> listByCourse(
            @Parameter(description = "Course ID", required = true, example = "1")
            @PathVariable Long courseId,
            Authentication auth, 
            @Parameter(description = "Pagination parameters (page, size, sort)")
            Pageable pageable) {
        return service.listByCourse(courseId, auth, pageable);
    }

    @Operation(
            summary = "Grade Submission",
            description = "Grade a student's assignment submission. Only instructors and admins can grade submissions.",
            operationId = "gradeSubmission"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Submission graded successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SubmissionResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Submission not found"),
            @ApiResponse(responseCode = "403", description = "Access denied - Instructor/Admin role required"),
            @ApiResponse(responseCode = "400", description = "Invalid grade data")
    })
    @RolesAllowed({"INSTRUCTOR","ADMIN"})
    @PatchMapping("/api/submissions/{id}/grade")
    public SubmissionResponse grade(
            @Parameter(description = "Submission ID to grade", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody GradeRequest req,
            Authentication auth) {
        return service.grade(id, req, auth);
    }

    @Operation(
            summary = "Request Resubmission",
            description = "Request a student to resubmit their assignment. " +
                    "Only instructors and admins can request resubmissions.",
            operationId = "requestResubmission"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Resubmission requested successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SubmissionResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Submission not found"),
            @ApiResponse(responseCode = "403", description = "Access denied - Instructor/Admin role required"),
            @ApiResponse(responseCode = "400", description = "Invalid resubmission request data")
    })
    @RolesAllowed({"INSTRUCTOR","ADMIN"})
    @PatchMapping("/api/submissions/{id}/request-resubmission")
    public SubmissionResponse requestResubmission(
            @Parameter(description = "Submission ID to request resubmission for", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ResubmitRequest req,
            Authentication auth) {
        return service.requestResubmission(id, req, auth);
    }
}
