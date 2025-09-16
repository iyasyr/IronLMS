package com.ironhack.lms.web.enrollment;

import com.ironhack.lms.service.enrollment.EnrollmentService;
import com.ironhack.lms.web.enrollment.dto.EnrollmentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Enrollments", description = "Course enrollment management endpoints for students and staff")
@SecurityRequirement(name = "bearerAuth")
public class EnrollmentController {

    private final EnrollmentService service;

    @Operation(
            summary = "Enroll in Course",
            description = "Enroll the authenticated student in a published course. Only students can enroll in courses.",
            operationId = "enrollInCourse"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully enrolled in course",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EnrollmentResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Course not found"),
            @ApiResponse(responseCode = "409", description = "Already enrolled in this course"),
            @ApiResponse(responseCode = "403", description = "Access denied - Student role required")
    })
    @RolesAllowed("STUDENT")
    @PostMapping("/api/courses/{courseId}/enroll")
    public ResponseEntity<EnrollmentResponse> enroll(
            @Parameter(description = "Course ID to enroll in", required = true, example = "1")
            @PathVariable Long courseId, 
            Authentication auth) {
        return ResponseEntity.ok(service.enroll(courseId, auth));
    }

    @Operation(
            summary = "Get My Enrollments",
            description = "Retrieve a paginated list of the authenticated student's course enrollments.",
            operationId = "getMyEnrollments"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved enrollments",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)
                    )
            ),
            @ApiResponse(responseCode = "403", description = "Access denied - Student role required")
    })
    @RolesAllowed("STUDENT")
    @GetMapping("/api/enrollments")
    public Page<EnrollmentResponse> myEnrollments(
            Authentication auth, 
            @Parameter(description = "Pagination parameters (page, size, sort)")
            Pageable pageable) {
        return service.myEnrollments(auth, pageable);
    }

    @Operation(
            summary = "Cancel Enrollment",
            description = "Cancel the authenticated student's enrollment in a course. " +
                    "Only the enrolled student can cancel their own enrollment.",
            operationId = "cancelEnrollment"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Enrollment cancelled successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EnrollmentResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Enrollment not found"),
            @ApiResponse(responseCode = "403", description = "Access denied - Not your enrollment")
    })
    @RolesAllowed("STUDENT")
    @PatchMapping("/api/enrollments/{id}/cancel")
    public EnrollmentResponse cancel(
            @Parameter(description = "Enrollment ID to cancel", required = true, example = "1")
            @PathVariable Long id, 
            Authentication auth) {
        return service.cancel(id, auth);
    }

    @Operation(
            summary = "Mark Enrollment as Complete",
            description = "Mark a student's enrollment as completed. Only instructors and admins can complete enrollments.",
            operationId = "completeEnrollment"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Enrollment marked as complete",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EnrollmentResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Enrollment not found"),
            @ApiResponse(responseCode = "403", description = "Access denied - Instructor/Admin role required")
    })
    @RolesAllowed({ "INSTRUCTOR", "ADMIN" })
    @PatchMapping("/api/enrollments/{id}/complete")
    public EnrollmentResponse complete(
            @Parameter(description = "Enrollment ID to mark as complete", required = true, example = "1")
            @PathVariable Long id, 
            Authentication auth) {
        return service.completeByStaff(id, auth);
    }
}
