package com.ironhack.lms.service.submission;

import com.ironhack.lms.domain.course.*;
import com.ironhack.lms.domain.submission.Submission;
import com.ironhack.lms.domain.submission.SubmissionStatus;
import com.ironhack.lms.domain.user.*;
import com.ironhack.lms.repository.course.AssignmentRepository;
import com.ironhack.lms.repository.course.CourseRepository;
import com.ironhack.lms.repository.enrollment.EnrollmentRepository;
import com.ironhack.lms.repository.submission.SubmissionRepository;
import com.ironhack.lms.repository.user.UserRepository;
import com.ironhack.lms.web.submission.dto.GradeRequest;
import com.ironhack.lms.web.submission.dto.ResubmitRequest;
import com.ironhack.lms.web.submission.dto.SubmissionCreateRequest;
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
class SubmissionServiceTest {

    @Mock(lenient = true) SubmissionRepository submissions;
    @Mock(lenient = true) AssignmentRepository assignments;
    @Mock(lenient = true) EnrollmentRepository enrollments;
    @Mock(lenient = true) CourseRepository courses;
    @Mock(lenient = true) UserRepository users;

    @InjectMocks SubmissionService service;

    Student student;
    Instructor instr;
    Course course;
    Assignment hw;

    TestingAuthenticationToken studentAuth;
    TestingAuthenticationToken instrAuth;

    @BeforeEach
    void setup() {
        student = new Student(); student.setId(30L); student.setEmail("s@lms.local"); student.setRole(Role.STUDENT);
        instr   = new Instructor(); instr.setId(40L); instr.setEmail("i@lms.local"); instr.setRole(Role.INSTRUCTOR);

        course = new Course();
        course.setId(200L); course.setInstructor(instr);
        course.setStatus(CourseStatus.PUBLISHED); course.setPublishedAt(Instant.now());

        hw = new Assignment(); hw.setId(300L); hw.setCourse(course); hw.setMaxPoints(100); hw.setAllowLate(true);

        when(users.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(users.findByEmail(instr.getEmail())).thenReturn(Optional.of(instr));
        when(assignments.findById(300L)).thenReturn(Optional.of(hw));

        studentAuth = new TestingAuthenticationToken(student.getEmail(), "x");
        instrAuth   = new TestingAuthenticationToken(instr.getEmail(), "x");
    }

    @Test
    void submit_ok_createsOrUpdates_and_versionBumps() {
        when(enrollments.existsByCourse_IdAndStudent_Id(200L, 30L)).thenReturn(true);
        when(submissions.findByAssignment_IdAndStudent_Id(300L, 30L)).thenReturn(Optional.empty());
        when(submissions.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var dto = service.submit(300L, new SubmissionCreateRequest("https://repo"), studentAuth);
        assertEquals(300L, dto.assignmentId());
        assertEquals(1, dto.version());

        // second submit -> version 2
        var existing = new Submission(); existing.setId(1L); existing.setAssignment(hw); existing.setStudent(student); existing.setVersion(1);
        when(submissions.findByAssignment_IdAndStudent_Id(300L, 30L)).thenReturn(Optional.of(existing));
        dto = service.submit(300L, new SubmissionCreateRequest("https://repo2"), studentAuth);
        assertEquals(2, dto.version());
    }

    @Test
    void submit_pastDue_disallowed_400() {
        when(enrollments.existsByCourse_IdAndStudent_Id(200L, 30L)).thenReturn(true);
        hw.setDueAt(Instant.now().minusSeconds(60)); hw.setAllowLate(false);

        var ex = assertThrows(ResponseStatusException.class,
                () -> service.submit(300L, new SubmissionCreateRequest("https://repo"), studentAuth));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void grade_scoreOutOfRange_400() {
        var s = new Submission(); s.setId(1L); s.setAssignment(hw); s.setStudent(student); s.setVersion(1);
        when(submissions.findById(1L)).thenReturn(Optional.of(s));
        when(courses.existsByIdAndInstructor_Id(200L, 40L)).thenReturn(true);

        var ex = assertThrows(ResponseStatusException.class,
                () -> service.grade(1L, new GradeRequest(9999, "no"), instrAuth));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void grade_byStudent_forbidden_403() {
        var s = new Submission(); s.setId(1L); s.setAssignment(hw); s.setStudent(student);
        when(submissions.findById(1L)).thenReturn(Optional.of(s));

        // deny instructor check
        when(courses.existsByIdAndInstructor_Id(200L, 30L)).thenReturn(false);

        var ex = assertThrows(ResponseStatusException.class,
                () -> service.grade(1L, new GradeRequest(50, "ok"), studentAuth));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void listByCourse_instructor_works() {
        when(courses.existsByIdAndInstructor_Id(200L, instr.getId())).thenReturn(true);
        Submission submission = new Submission();
        submission.setId(1L);
        submission.setAssignment(hw);
        submission.setStudent(student);
        submission.setSubmittedAt(Instant.now());
        submission.setArtifactUrl("http://example.com/artifact");
        submission.setStatus(SubmissionStatus.SUBMITTED);
        submission.setVersion(1);

        when(submissions.findByAssignment_Course_Id(eq(200L), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(submission)));

        var result = service.listByCourse(200L, instrAuth, PageRequest.of(0, 10));
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void mySubmissions_student_works() {
        Submission submission = new Submission();
        submission.setId(1L);
        submission.setAssignment(hw);
        submission.setStudent(student);
        submission.setSubmittedAt(Instant.now());
        submission.setArtifactUrl("http://example.com/artifact");
        submission.setStatus(SubmissionStatus.SUBMITTED);
        submission.setVersion(1);

        when(submissions.findByStudent_Id(eq(30L), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(submission)));

        var result = service.mySubmissions(studentAuth, PageRequest.of(0, 10));
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void mySubmissions_nonStudent_forbidden() {
        var ex = assertThrows(ResponseStatusException.class, 
                () -> service.mySubmissions(instrAuth, PageRequest.of(0, 10)));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void requestResubmission_instructor_works() {
        var s = new Submission(); 
        s.setId(1L); 
        s.setAssignment(hw); 
        s.setStudent(student); 
        s.setStatus(SubmissionStatus.SUBMITTED);
        s.setVersion(1);
        
        when(submissions.findById(1L)).thenReturn(Optional.of(s));
        when(courses.existsByIdAndInstructor_Id(200L, 40L)).thenReturn(true);
        when(submissions.save(any(Submission.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = service.requestResubmission(1L, new ResubmitRequest("Please improve"), instrAuth);
        assertEquals(SubmissionStatus.RESUBMIT_REQUESTED, s.getStatus());
        assertEquals("Please improve", s.getFeedback());
        assertNotNull(result);
    }

    @Test
    void requestResubmission_student_forbidden() {
        var s = new Submission(); 
        s.setId(1L); 
        s.setAssignment(hw); 
        s.setStudent(student);
        
        when(submissions.findById(1L)).thenReturn(Optional.of(s));
        when(courses.existsByIdAndInstructor_Id(200L, 30L)).thenReturn(false);

        var ex = assertThrows(ResponseStatusException.class,
                () -> service.requestResubmission(1L, new ResubmitRequest("test"), studentAuth));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void requestResubmission_nonExistentSubmission_notFound() {
        when(submissions.findById(999L)).thenReturn(Optional.empty());
        
        var ex = assertThrows(ResponseStatusException.class,
                () -> service.requestResubmission(999L, new ResubmitRequest("test"), instrAuth));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}
