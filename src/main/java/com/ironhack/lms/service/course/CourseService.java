package com.ironhack.lms.service.course;

import com.ironhack.lms.domain.course.*;
import com.ironhack.lms.domain.enrollment.EnrollmentStatus;
import com.ironhack.lms.domain.user.Instructor;
import com.ironhack.lms.domain.user.Role;
import com.ironhack.lms.domain.user.Student;
import com.ironhack.lms.domain.user.User;
import com.ironhack.lms.repository.course.*;
import com.ironhack.lms.repository.enrollment.EnrollmentRepository;
import com.ironhack.lms.repository.user.UserRepository;
import com.ironhack.lms.web.course.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courses;
    private final LessonRepository lessons;
    private final AssignmentRepository assignments;
    private final UserRepository users;
    private final EnrollmentRepository enrollments;

    // --- Queries ---

    public Page<CourseResponse> listPublished(Pageable p) {
        return courses.findByStatus(CourseStatus.PUBLISHED, p).map(this::toDto);
    }

    public CourseResponse getForRead(Long id, Authentication auth) {
        Course c = courses.findById(id).orElseThrow(() -> notFound("Course"));
        if (c.getStatus() == CourseStatus.PUBLISHED) return toDto(c);

        // allow owner/instructor or admin to see drafts
        if (auth != null) {
            User u = users.findByEmail(auth.getName()).orElse(null);
            if (u != null && (u.getRole() == Role.ADMIN ||
                    (u.getRole() == Role.INSTRUCTOR && c.getInstructor().getId().equals(u.getId())))) {
                return toDto(c);
            }
        }
        throw notFound("Course");
    }

    // --- Commands ---

    public CourseResponse createCourse(CourseCreateRequest req, Authentication auth) {
        User u = users.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        if (!(u instanceof Instructor instructor))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only instructors can create courses");

        Course c = new Course();
        c.setInstructor(instructor);
        c.setTitle(req.title());
        c.setDescription(req.description());
        c.setStatus(CourseStatus.DRAFT);
        c = courses.save(c);
        return toDto(c);
    }

    public CourseResponse updateCourse(Long id, CourseUpdateRequest req, Authentication auth) {
        Course c = courses.findById(id).orElseThrow(() -> notFound("Course"));
        requireOwnerOrAdmin(auth, c);

        c.setTitle(req.title());
        c.setDescription(req.description());
        c.setStatus(req.status());
        if (req.status() == CourseStatus.PUBLISHED && c.getPublishedAt() == null) {
            c.setPublishedAt(Instant.now());
        }
        if (req.status() != CourseStatus.PUBLISHED) {
            c.setPublishedAt(null);
        }
        return toDto(courses.save(c));
    }

    public void deleteCourse(Long id, Authentication auth) {
        Course c = courses.findById(id).orElseThrow(() -> notFound("Course"));
        requireOwnerOrAdmin(auth, c);
        courses.delete(c);
    }

    public Long addLesson(Long courseId, LessonCreateRequest req, Authentication auth) {
        Course c = courses.findById(courseId).orElseThrow(() -> notFound("Course"));
        requireOwnerOrAdmin(auth, c);
        Lesson l = new Lesson();
        l.setCourse(c);
        l.setTitle(req.title());
        l.setContentUrl(req.contentUrl());
        l.setOrderIndex(req.orderIndex());
        return lessons.save(l).getId();
    }

    public Long addAssignment(Long courseId, AssignmentCreateRequest req, Authentication auth) {
        Course c = courses.findById(courseId).orElseThrow(() -> notFound("Course"));
        requireOwnerOrAdmin(auth, c);
        Assignment a = new Assignment();
        a.setCourse(c);
        a.setTitle(req.title());
        a.setInstructions(req.instructions());
        a.setDueAt(req.dueAt());
        a.setMaxPoints(req.maxPoints());
        a.setAllowLate(req.allowLate());
        return assignments.save(a).getId();
    }

    public void updateLesson(Long courseId, Long lessonId, LessonUpdateRequest req, Authentication auth) {
        Course c = courses.findById(courseId).orElseThrow(() -> notFound("Course"));
        requireOwnerOrAdmin(auth, c);
        Lesson l = lessons.findById(lessonId).orElseThrow(() -> notFound("Lesson"));
        if (!l.getCourse().getId().equals(courseId)) throw notFound("Lesson"); // hide cross-course
        l.setTitle(req.title());
        l.setContentUrl(req.contentUrl());
        l.setOrderIndex(req.orderIndex());
        lessons.save(l);
    }

    public void deleteLesson(Long courseId, Long lessonId, Authentication auth) {
        Course c = courses.findById(courseId).orElseThrow(() -> notFound("Course"));
        requireOwnerOrAdmin(auth, c);
        Lesson l = lessons.findById(lessonId).orElseThrow(() -> notFound("Lesson"));
        if (!l.getCourse().getId().equals(courseId)) throw notFound("Lesson");
        lessons.delete(l);
    }

    public void updateAssignment(Long courseId, Long assignmentId, AssignmentUpdateRequest req, Authentication auth) {
        Course c = courses.findById(courseId).orElseThrow(() -> notFound("Course"));
        requireOwnerOrAdmin(auth, c);
        Assignment a = assignments.findById(assignmentId).orElseThrow(() -> notFound("Assignment"));
        if (!a.getCourse().getId().equals(courseId)) throw notFound("Assignment");
        a.setTitle(req.title());
        a.setInstructions(req.instructions());
        a.setDueAt(req.dueAt());
        a.setMaxPoints(req.maxPoints());
        a.setAllowLate(req.allowLate());
        assignments.save(a);
    }

    public void deleteAssignment(Long courseId, Long assignmentId, Authentication auth) {
        Course c = courses.findById(courseId).orElseThrow(() -> notFound("Course"));
        requireOwnerOrAdmin(auth, c);
        Assignment a = assignments.findById(assignmentId).orElseThrow(() -> notFound("Assignment"));
        if (!a.getCourse().getId().equals(courseId)) throw notFound("Assignment");
        assignments.delete(a);
    }

    // --- helpers ---

    private void requireOwnerOrAdmin(Authentication auth, Course c) {
        if (auth == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        User u = users.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        boolean ok = u.getRole() == Role.ADMIN ||
                (u.getRole() == Role.INSTRUCTOR && c.getInstructor().getId().equals(u.getId()));
        if (!ok) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    private ResponseStatusException notFound(String what) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, what + " not found");
    }

    private CourseResponse toDto(Course c) {
        return new CourseResponse(
                c.getId(),
                c.getInstructor().getId(),
                c.getTitle(),
                c.getDescription(),
                c.getStatus(),
                c.getCreatedAt(),
                c.getPublishedAt()
        );
    }

    public java.util.List<LessonSummaryResponse> listLessonsForRead(Long courseId, Authentication auth) {
        Course c = courses.findById(courseId).orElseThrow(() -> notFound("Course"));
        
        // If course is published, check if user is enrolled or is instructor/admin
        if (c.getStatus() == CourseStatus.PUBLISHED) {
            if (auth != null) {
                User u = users.findByEmail(auth.getName()).orElse(null);
                if (u != null) {
                    // Allow instructors and admins to see lessons
                    if (u.getRole() == Role.ADMIN || 
                        (u.getRole() == Role.INSTRUCTOR && c.getInstructor().getId().equals(u.getId()))) {
                        return lessons.findByCourse_IdOrderByOrderIndexAsc(courseId).stream()
                                .map(l -> new LessonSummaryResponse(l.getId(), l.getTitle(), l.getContentUrl(), l.getOrderIndex()))
                                .toList();
                    }
                    // For students, check if enrolled
                    if (u.getRole() == Role.STUDENT && u instanceof Student student) {
                        // Check if student is enrolled and enrollment is active
                        boolean isEnrolled = enrollments.existsByCourse_IdAndStudent_IdAndStatus(
                                courseId, student.getId(), EnrollmentStatus.ACTIVE);
                        if (!isEnrolled) {
                            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Must be enrolled to access lessons");
                        }
                        return lessons.findByCourse_IdOrderByOrderIndexAsc(courseId).stream()
                                .map(l -> new LessonSummaryResponse(l.getId(), l.getTitle(), l.getContentUrl(), l.getOrderIndex()))
                                .toList();
                    }
                    // If user is not instructor/admin/student, deny access
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Must be enrolled to access lessons");
                }
            }
            // If not authenticated, don't allow access to lessons
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Must be enrolled to access lessons");
        } else {
            // For draft courses, only owner/instructor or admin can see
            requireOwnerOrAdmin(auth, c);
            return lessons.findByCourse_IdOrderByOrderIndexAsc(courseId).stream()
                    .map(l -> new LessonSummaryResponse(l.getId(), l.getTitle(), l.getContentUrl(), l.getOrderIndex()))
                    .toList();
        }
    }

    public java.util.List<AssignmentSummaryResponse> listAssignmentsForRead(Long courseId, Authentication auth) {
        Course c = courses.findById(courseId).orElseThrow(() -> notFound("Course"));
        
        // If course is published, check if user is enrolled or is instructor/admin
        if (c.getStatus() == CourseStatus.PUBLISHED) {
            if (auth != null) {
                User u = users.findByEmail(auth.getName()).orElse(null);
                if (u != null) {
                    // Allow instructors and admins to see assignments
                    if (u.getRole() == Role.ADMIN || 
                        (u.getRole() == Role.INSTRUCTOR && c.getInstructor().getId().equals(u.getId()))) {
                        return assignments.findByCourse_Id(courseId).stream()
                                .map(a -> new AssignmentSummaryResponse(a.getId(), a.getTitle(), a.getInstructions(),
                                        a.getMaxPoints(), a.isAllowLate(), a.getDueAt()))
                                .toList();
                    }
                    // For students, check if enrolled
                    if (u.getRole() == Role.STUDENT && u instanceof Student student) {
                        // Check if student is enrolled and enrollment is active
                        boolean isEnrolled = enrollments.existsByCourse_IdAndStudent_IdAndStatus(
                                courseId, student.getId(), EnrollmentStatus.ACTIVE);
                        if (!isEnrolled) {
                            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Must be enrolled to access assignments");
                        }
                        return assignments.findByCourse_Id(courseId).stream()
                                .map(a -> new AssignmentSummaryResponse(a.getId(), a.getTitle(), a.getInstructions(),
                                        a.getMaxPoints(), a.isAllowLate(), a.getDueAt()))
                                .toList();
                    }
                    // If user is not instructor/admin/student, deny access
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Must be enrolled to access assignments");
                }
            }
            // If not authenticated, don't allow access to assignments
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Must be enrolled to access assignments");
        } else {
            // For draft courses, only owner/instructor or admin can see
            requireOwnerOrAdmin(auth, c);
            return assignments.findByCourse_Id(courseId).stream()
                    .map(a -> new AssignmentSummaryResponse(a.getId(), a.getTitle(), a.getInstructions(),
                            a.getMaxPoints(), a.isAllowLate(), a.getDueAt()))
                    .toList();
        }
    }
}
