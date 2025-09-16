package com.ironhack.lms.service.enrollment;

import com.ironhack.lms.domain.course.Course;
import com.ironhack.lms.domain.course.CourseStatus;
import com.ironhack.lms.domain.enrollment.Enrollment;
import com.ironhack.lms.domain.enrollment.EnrollmentStatus;
import com.ironhack.lms.domain.user.Instructor;
import com.ironhack.lms.domain.user.Role;
import com.ironhack.lms.domain.user.Student;
import com.ironhack.lms.repository.course.CourseRepository;
import com.ironhack.lms.repository.enrollment.EnrollmentRepository;
import com.ironhack.lms.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock(lenient = true) EnrollmentRepository enrollments;
    @Mock(lenient = true) CourseRepository courses;
    @Mock(lenient = true) UserRepository users;

    @InjectMocks EnrollmentService service;

    Student student;
    Course course;
    Instructor instructor;
    Instructor admin;
    TestingAuthenticationToken studentAuth;
    TestingAuthenticationToken instructorAuth;
    TestingAuthenticationToken adminAuth;

    @BeforeEach
    void setup() {
        student = new Student();
        student.setId(20L);
        student.setEmail("student@lms.local");
        student.setRole(Role.STUDENT);

        instructor = new Instructor();
        instructor.setId(30L);
        instructor.setEmail("instructor@lms.local");
        instructor.setRole(Role.INSTRUCTOR);

        admin = new Instructor();
        admin.setId(40L);
        admin.setEmail("admin@lms.local");
        admin.setRole(Role.ADMIN);

        course = new Course();
        course.setId(100L);
        course.setStatus(CourseStatus.PUBLISHED);
        course.setPublishedAt(Instant.now());
        course.setInstructor(instructor);

        when(users.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(users.findByEmail(instructor.getEmail())).thenReturn(Optional.of(instructor));
        when(users.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        when(courses.findById(100L)).thenReturn(Optional.of(course));

        studentAuth = new TestingAuthenticationToken(student.getEmail(), "n/a");
        instructorAuth = new TestingAuthenticationToken(instructor.getEmail(), "n/a");
        adminAuth = new TestingAuthenticationToken(admin.getEmail(), "n/a");
    }

    @Test
    void enroll_firstTime_ok() {
        when(enrollments.existsByCourse_IdAndStudent_Id(100L, 20L)).thenReturn(false);
        
        when(enrollments.save(any(Enrollment.class))).thenAnswer(invocation -> {
            Enrollment e = invocation.getArgument(0);
            e.setId(1L);
            e.setStatus(EnrollmentStatus.ACTIVE);
            e.setEnrolledAt(Instant.now());
            return e;
        });
        
        assertDoesNotThrow(() -> service.enroll(100L, studentAuth));
        verify(enrollments).save(any());
    }

    @Test
    void enroll_duplicate_conflict409() {
        when(enrollments.existsByCourse_IdAndStudent_Id(100L, 20L)).thenReturn(true);
        var ex = assertThrows(ResponseStatusException.class, () -> service.enroll(100L, studentAuth));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void enroll_draftCourse_notFound() {
        course.setStatus(CourseStatus.DRAFT);
        var ex = assertThrows(ResponseStatusException.class, () -> service.enroll(100L, studentAuth));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void enroll_nonExistentCourse_notFound() {
        when(courses.findById(999L)).thenReturn(Optional.empty());
        var ex = assertThrows(ResponseStatusException.class, () -> service.enroll(999L, studentAuth));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void enroll_nullAuth_unauthorized() {
        var ex = assertThrows(ResponseStatusException.class, () -> service.enroll(100L, null));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void enroll_nonStudent_forbidden() {
        var ex = assertThrows(ResponseStatusException.class, () -> service.enroll(100L, instructorAuth));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void myEnrollments_happyPath_returnsList() {
        Enrollment enrollment = new Enrollment();
        enrollment.setId(1L);
        enrollment.setCourse(course);
        enrollment.setStudent(student);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollment.setEnrolledAt(Instant.now());

        when(enrollments.findByStudent_Id(eq(20L), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(enrollment)));

        var result = service.myEnrollments(studentAuth, PageRequest.of(0, 10));
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void cancel_happyPath_cancels() {
        Enrollment enrollment = new Enrollment();
        enrollment.setId(1L);
        enrollment.setCourse(course);
        enrollment.setStudent(student);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        when(enrollments.findByIdAndStudent_Id(1L, 20L)).thenReturn(Optional.of(enrollment));
        when(enrollments.save(any(Enrollment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.cancel(1L, studentAuth);
        assertEquals(EnrollmentStatus.CANCELLED, enrollment.getStatus());
        assertNotNull(result);
    }

    @Test
    void cancel_notOwnEnrollment_notFound() {
        when(enrollments.findByIdAndStudent_Id(1L, 20L)).thenReturn(Optional.empty());
        var ex = assertThrows(ResponseStatusException.class, () -> service.cancel(1L, studentAuth));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void completeByStaff_admin_works() {
        Enrollment enrollment = new Enrollment();
        enrollment.setId(1L);
        enrollment.setCourse(course);
        enrollment.setStudent(student);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        
        when(enrollments.findById(1L)).thenReturn(Optional.of(enrollment));
        when(enrollments.save(any(Enrollment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.completeByStaff(1L, adminAuth);
        assertEquals(EnrollmentStatus.COMPLETED, enrollment.getStatus());
        assertNotNull(result);
    }

    @Test
    void completeByStaff_ownerInstructor_works() {
        Enrollment enrollment = new Enrollment();
        enrollment.setId(1L);
        enrollment.setCourse(course);
        enrollment.setStudent(student);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        
        when(enrollments.findById(1L)).thenReturn(Optional.of(enrollment));
        when(enrollments.save(any(Enrollment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.completeByStaff(1L, instructorAuth);
        assertEquals(EnrollmentStatus.COMPLETED, enrollment.getStatus());
        assertNotNull(result);
    }

    @Test
    void completeByStaff_student_forbidden() {
        Enrollment enrollment = new Enrollment();
        enrollment.setId(1L);
        enrollment.setCourse(course);
        enrollment.setStudent(student);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        
        when(enrollments.findById(1L)).thenReturn(Optional.of(enrollment));

        var ex = assertThrows(ResponseStatusException.class, () -> service.completeByStaff(1L, studentAuth));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void completeByStaff_nonOwnerInstructor_forbidden() {
        // Different instructor who doesn't own the course
        Instructor otherInstructor = new Instructor();
        otherInstructor.setId(99L);
        otherInstructor.setEmail("other@lms.local");
        otherInstructor.setRole(Role.INSTRUCTOR);
        when(users.findByEmail(otherInstructor.getEmail())).thenReturn(Optional.of(otherInstructor));
        TestingAuthenticationToken otherInstructorAuth = new TestingAuthenticationToken(otherInstructor.getEmail(), "n/a");

        Enrollment enrollment = new Enrollment();
        enrollment.setId(1L);
        enrollment.setCourse(course);
        enrollment.setStudent(student);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        
        when(enrollments.findById(1L)).thenReturn(Optional.of(enrollment));

        var ex = assertThrows(ResponseStatusException.class, () -> service.completeByStaff(1L, otherInstructorAuth));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void completeByStaff_nonExistentEnrollment_notFound() {
        when(enrollments.findById(999L)).thenReturn(Optional.empty());
        var ex = assertThrows(ResponseStatusException.class, () -> service.completeByStaff(999L, adminAuth));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void completeByStaff_unknownUser_unauthorized() {
        when(users.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
        TestingAuthenticationToken unknownAuth = new TestingAuthenticationToken("unknown@example.com", "n/a");
        
        var ex = assertThrows(ResponseStatusException.class, () -> service.completeByStaff(1L, unknownAuth));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }
}
