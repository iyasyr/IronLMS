package com.ironhack.lms.service.course;

import com.ironhack.lms.domain.course.*;
import com.ironhack.lms.domain.user.Instructor;
import com.ironhack.lms.domain.user.Role;
import com.ironhack.lms.repository.course.AssignmentRepository;
import com.ironhack.lms.repository.course.CourseRepository;
import com.ironhack.lms.repository.course.LessonRepository;
import com.ironhack.lms.repository.user.UserRepository;
import com.ironhack.lms.web.course.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock(lenient = true) CourseRepository courses;
    @Mock(lenient = true) LessonRepository lessons;
    @Mock(lenient = true) AssignmentRepository assignments;
    @Mock(lenient = true) UserRepository users;

    @InjectMocks CourseService service;

    Instructor owner;
    Course course;
    TestingAuthenticationToken ownerAuth;

    @BeforeEach
    void setUp() {
        owner = new Instructor();
        owner.setId(10L);
        owner.setEmail("owner@lms.local");
        owner.setRole(Role.INSTRUCTOR);

        course = new Course();
        course.setId(100L);
        course.setInstructor(owner);
        course.setTitle("T");
        course.setStatus(CourseStatus.PUBLISHED);
        course.setPublishedAt(Instant.now());

        ownerAuth = new TestingAuthenticationToken(owner.getEmail(), "n/a");
        when(users.findByEmail(owner.getEmail())).thenReturn(Optional.of(owner));
        when(courses.findById(100L)).thenReturn(Optional.of(course));
    }

    @Test
    void createCourse_happyPath_creates() {
        when(courses.save(any(Course.class))).thenAnswer(inv -> {
            Course c = inv.getArgument(0);
            c.setId(999L);
            c.setCreatedAt(Instant.now());
            return c;
        });
        
        var req = new CourseCreateRequest("New Course", "Description");
        var result = service.createCourse(req, ownerAuth);
        
        assertEquals(999L, result.id());
        assertEquals("New Course", result.title());
        assertEquals(CourseStatus.DRAFT, result.status());
        verify(courses).save(any(Course.class));
    }

    @Test
    void updateCourse_happyPath_updates() {
        when(courses.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));
        
        var req = new CourseUpdateRequest("Updated", "New desc", CourseStatus.PUBLISHED);
        var result = service.updateCourse(100L, req, ownerAuth);
        
        assertEquals("Updated", course.getTitle());
        assertEquals("New desc", course.getDescription());
        assertEquals(CourseStatus.PUBLISHED, course.getStatus());
        assertNotNull(course.getPublishedAt());
        verify(courses).save(course);
    }

    @Test
    void deleteCourse_happyPath_deletes() {
        assertDoesNotThrow(() -> service.deleteCourse(100L, ownerAuth));
        verify(courses).delete(course);
    }

    @Test
    void getForRead_published_works() {
        var result = service.getForRead(100L, null);
        assertEquals(100L, result.id());
        assertEquals("T", result.title());
    }

    @Test
    void addLesson_happyPath_creates() {
        when(lessons.save(any(Lesson.class))).thenAnswer(inv -> {
            Lesson l = inv.getArgument(0);
            l.setId(50L);
            return l;
        });
        
        var req = new LessonCreateRequest("Lesson 1", "https://video.com", 1);
        var lessonId = service.addLesson(100L, req, ownerAuth);
        
        assertEquals(50L, lessonId);
        verify(lessons).save(any(Lesson.class));
    }

    @Test
    void addAssignment_happyPath_creates() {
        when(assignments.save(any(Assignment.class))).thenAnswer(inv -> {
            Assignment a = inv.getArgument(0);
            a.setId(60L);
            return a;
        });
        
        var req = new AssignmentCreateRequest("HW1", "Instructions", null, 100, true);
        var assignmentId = service.addAssignment(100L, req, ownerAuth);
        
        assertEquals(60L, assignmentId);
        verify(assignments).save(any(Assignment.class));
    }

    @Test
    void updateLesson_happyPath_saves() {
        Lesson l = new Lesson();
        l.setId(5L); l.setCourse(course); l.setTitle("old"); l.setOrderIndex(1);
        when(lessons.findById(5L)).thenReturn(Optional.of(l));

        var req = new LessonUpdateRequest("new", "https://x", 2);
        assertDoesNotThrow(() -> service.updateLesson(100L, 5L, req, ownerAuth));

        assertEquals("new", l.getTitle());
        assertEquals(2, l.getOrderIndex());
        verify(lessons).save(l);
    }

    @Test
    void deleteAssignment_happyPath_deletes() {
        Assignment a = new Assignment();
        a.setId(7L); a.setCourse(course);
        when(assignments.findById(7L)).thenReturn(Optional.of(a));

        assertDoesNotThrow(() -> service.deleteAssignment(100L, 7L, ownerAuth));
        verify(assignments).delete(a);
    }

    @Test
    void listLessonsForRead_published_works() {
        Lesson lesson = new Lesson();
        lesson.setId(1L);
        lesson.setTitle("Lesson 1");
        lesson.setContentUrl("https://example.com");
        lesson.setOrderIndex(1);
        
        when(lessons.findByCourse_IdOrderByOrderIndexAsc(100L))
            .thenReturn(List.of(lesson));
        
        var result = service.listLessonsForRead(100L, null);
        assertEquals(1, result.size());
        assertEquals("Lesson 1", result.get(0).title());
    }

    @Test
    void listAssignmentsForRead_published_works() {
        Assignment assignment = new Assignment();
        assignment.setId(1L);
        assignment.setTitle("HW1");
        assignment.setInstructions("Do this");
        assignment.setMaxPoints(100);
        assignment.setAllowLate(true);
        
        when(assignments.findByCourse_Id(100L))
            .thenReturn(List.of(assignment));
        
        var result = service.listAssignmentsForRead(100L, null);
        assertEquals(1, result.size());
        assertEquals("HW1", result.get(0).title());
    }
}
