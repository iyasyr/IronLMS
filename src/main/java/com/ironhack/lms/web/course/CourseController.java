package com.ironhack.lms.web.course;

import com.ironhack.lms.service.course.CourseService;
import com.ironhack.lms.web.course.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
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
@Tag(name = "Courses", description = "Course management endpoints for creating, reading, updating, and deleting courses")
@SecurityRequirement(name = "bearerAuth")
public class CourseController {

    private final CourseService service;

    // ---- Public reads ----
    @Operation(
            summary = "List Published Courses",
            description = "Retrieve a paginated list of all published courses. This endpoint is publicly accessible.",
            operationId = "listPublishedCourses"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved list of published courses",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)
                    )
            )
    })
    @PermitAll
    @GetMapping
    public Page<CourseResponse> listPublished(
            @Parameter(description = "Pagination parameters (page, size, sort)")
            Pageable pageable) {
        return service.listPublished(pageable);
    }

    @Operation(
            summary = "Get Course Details",
            description = "Retrieve detailed information about a specific course. " +
                    "Students see basic info, instructors see additional details.",
            operationId = "getCourse"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved course details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CourseResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Course not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Course Not Found",
                                    value = "{\"error\": \"Course not found\"}"
                            )
                    )
            )
    })
    @PermitAll
    @GetMapping("/{id}")
    public CourseResponse get(
            @Parameter(description = "Course ID", required = true, example = "1")
            @PathVariable Long id, 
            Authentication auth) {
        return service.getForRead(id, auth);
    }

    // ---- Writes (instructor/admin) ----
    @Operation(
            summary = "Create New Course",
            description = "Create a new course. Only instructors can create courses. " +
                    "The course will be created in DRAFT status initially.",
            operationId = "createCourse"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Course created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CourseResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Validation Error",
                                    value = "{\"error\": \"Title is required\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Instructor role required",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Access Denied",
                                    value = "{\"error\": \"forbidden\"}"
                            )
                    )
            )
    })
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping
    public CourseResponse create(
            @Valid @RequestBody CourseCreateRequest req, 
            Authentication auth) {
        return service.createCourse(req, auth);
    }

    @Operation(
            summary = "Update Course",
            description = "Update an existing course. Instructors can update their own courses, " +
                    "admins can update any course.",
            operationId = "updateCourse"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Course updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CourseResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Course not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Course Not Found",
                                    value = "{\"error\": \"Course not found\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Not authorized to update this course",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Access Denied",
                                    value = "{\"error\": \"forbidden\"}"
                            )
                    )
            )
    })
    @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
    @PutMapping("/{id}")
    public CourseResponse update(
            @Parameter(description = "Course ID", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody CourseUpdateRequest req,
            Authentication auth) {
        return service.updateCourse(id, req, auth);
    }

    @Operation(
            summary = "Delete Course",
            description = "Delete a course permanently. Instructors can delete their own courses, " +
                    "admins can delete any course.",
            operationId = "deleteCourse"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Course deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Course not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Course Not Found",
                                    value = "{\"error\": \"Course not found\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Not authorized to delete this course",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Access Denied",
                                    value = "{\"error\": \"forbidden\"}"
                            )
                    )
            )
    })
    @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Course ID", required = true, example = "1")
            @PathVariable Long id, 
            Authentication auth) {
        service.deleteCourse(id, auth);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Add Lesson to Course",
            description = "Add a new lesson to an existing course. Only instructors and admins can add lessons.",
            operationId = "addLesson"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lesson added successfully"),
            @ApiResponse(responseCode = "404", description = "Course not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
    @PostMapping("/{id}/lessons")
    public ResponseEntity<Long> addLesson(
            @Parameter(description = "Course ID", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody LessonCreateRequest req,
            Authentication auth) {
        return ResponseEntity.ok(service.addLesson(id, req, auth));
    }

    @Operation(
            summary = "Add Assignment to Course",
            description = "Add a new assignment to an existing course. Only instructors and admins can add assignments.",
            operationId = "addAssignment"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Assignment added successfully"),
            @ApiResponse(responseCode = "404", description = "Course not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
    @PostMapping("/{id}/assignments")
    public ResponseEntity<Long> addAssignment(
            @Parameter(description = "Course ID", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody AssignmentCreateRequest req,
            Authentication auth) {
        return ResponseEntity.ok(service.addAssignment(id, req, auth));
    }

    @Operation(
            summary = "Update Lesson",
            description = "Update an existing lesson in a course. Only instructors and admins can update lessons.",
            operationId = "updateLesson"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Lesson updated successfully"),
            @ApiResponse(responseCode = "404", description = "Course or lesson not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
    @PutMapping("/{courseId}/lessons/{lessonId}")
    public ResponseEntity<Void> updateLesson(
            @Parameter(description = "Course ID", required = true, example = "1")
            @PathVariable Long courseId,
            @Parameter(description = "Lesson ID", required = true, example = "1")
            @PathVariable Long lessonId,
            @Valid @RequestBody LessonUpdateRequest req,
            Authentication auth) {
        service.updateLesson(courseId, lessonId, req, auth);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Delete Lesson",
            description = "Delete a lesson from a course. Only instructors and admins can delete lessons.",
            operationId = "deleteLesson"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Lesson deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Course or lesson not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
    @DeleteMapping("/{courseId}/lessons/{lessonId}")
    public ResponseEntity<Void> deleteLesson(
            @Parameter(description = "Course ID", required = true, example = "1")
            @PathVariable Long courseId,
            @Parameter(description = "Lesson ID", required = true, example = "1")
            @PathVariable Long lessonId,
            Authentication auth) {
        service.deleteLesson(courseId, lessonId, auth);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Update Assignment",
            description = "Update an existing assignment in a course. Only instructors and admins can update assignments.",
            operationId = "updateAssignment"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Assignment updated successfully"),
            @ApiResponse(responseCode = "404", description = "Course or assignment not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
    @PutMapping("/{courseId}/assignments/{assignmentId}")
    public ResponseEntity<Void> updateAssignment(
            @Parameter(description = "Course ID", required = true, example = "1")
            @PathVariable Long courseId,
            @Parameter(description = "Assignment ID", required = true, example = "1")
            @PathVariable Long assignmentId,
            @Valid @RequestBody AssignmentUpdateRequest req,
            Authentication auth) {
        service.updateAssignment(courseId, assignmentId, req, auth);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Delete Assignment",
            description = "Delete an assignment from a course. Only instructors and admins can delete assignments.",
            operationId = "deleteAssignment"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Assignment deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Course or assignment not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
    @DeleteMapping("/{courseId}/assignments/{assignmentId}")
    public ResponseEntity<Void> deleteAssignment(
            @Parameter(description = "Course ID", required = true, example = "1")
            @PathVariable Long courseId,
            @Parameter(description = "Assignment ID", required = true, example = "1")
            @PathVariable Long assignmentId,
            Authentication auth) {
        service.deleteAssignment(courseId, assignmentId, auth);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "List Course Lessons",
            description = "Get a list of all lessons in a course. Students must be enrolled to access lessons.",
            operationId = "listCourseLessons"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved course lessons",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = java.util.List.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Course not found"),
            @ApiResponse(responseCode = "403", description = "Access denied - Must be enrolled or instructor")
    })
    @RolesAllowed({"STUDENT", "INSTRUCTOR", "ADMIN"})
    @GetMapping("/{id}/lessons")
    public java.util.List<LessonSummaryResponse> lessons(
            @Parameter(description = "Course ID", required = true, example = "1")
            @PathVariable Long id, 
            Authentication auth) {
        return service.listLessonsForRead(id, auth);
    }

    @Operation(
            summary = "List Course Assignments",
            description = "Get a list of all assignments in a course. Students must be enrolled to access assignments.",
            operationId = "listCourseAssignments"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved course assignments",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = java.util.List.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Course not found"),
            @ApiResponse(responseCode = "403", description = "Access denied - Must be enrolled or instructor")
    })
    @RolesAllowed({"STUDENT", "INSTRUCTOR", "ADMIN"})
    @GetMapping("/{id}/assignments")
    public java.util.List<AssignmentSummaryResponse> assignments(
            @Parameter(description = "Course ID", required = true, example = "1")
            @PathVariable Long id, 
            Authentication auth) {
        return service.listAssignmentsForRead(id, auth);
    }
}
