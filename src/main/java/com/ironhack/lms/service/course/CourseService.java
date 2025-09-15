package com.ironhack.lms.service.course;

import com.ironhack.lms.domain.course.*;
import com.ironhack.lms.domain.user.Instructor;
import com.ironhack.lms.domain.user.Role;
import com.ironhack.lms.domain.user.User;
import com.ironhack.lms.repository.course.*;
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
}
