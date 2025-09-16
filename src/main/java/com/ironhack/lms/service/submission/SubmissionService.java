package com.ironhack.lms.service.submission;

import com.ironhack.lms.domain.course.Assignment;
import com.ironhack.lms.domain.course.Course;
import com.ironhack.lms.domain.submission.Submission;
import com.ironhack.lms.domain.submission.SubmissionStatus;
import com.ironhack.lms.domain.user.*;
import com.ironhack.lms.repository.course.AssignmentRepository;
import com.ironhack.lms.repository.course.CourseRepository;
import com.ironhack.lms.repository.enrollment.EnrollmentRepository;
import com.ironhack.lms.repository.submission.SubmissionRepository;
import com.ironhack.lms.repository.user.UserRepository;
import com.ironhack.lms.web.submission.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissions;
    private final AssignmentRepository assignments;
    private final EnrollmentRepository enrollments;
    private final UserRepository users;
    private final CourseRepository courses;

    // ----- Student actions -----

    @Transactional
    public SubmissionResponse submit(Long assignmentId, SubmissionCreateRequest req, Authentication auth) {
        Student me = requireStudent(auth);

        Assignment a = assignments.findById(assignmentId)
                .orElseThrow(() -> notFound("Assignment"));
        Course c = a.getCourse();

        // Must be published course
        if (c.getStatus() != com.ironhack.lms.domain.course.CourseStatus.PUBLISHED) {
            throw notFound("Assignment");
        }
        // Must be enrolled & active
        boolean enrolled = enrollments.existsByCourse_IdAndStudent_Id(c.getId(), me.getId());
        if (!enrolled) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not enrolled in this course");

        // Due date + late policy
        if (a.getDueAt() != null && Instant.now().isAfter(a.getDueAt()) && !a.isAllowLate()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignment is past due and late submissions are disabled");
        }

        Submission s = submissions.findByAssignment_IdAndStudent_Id(assignmentId, me.getId())
                .orElseGet(Submission::new);

        s.setAssignment(a);
        s.setStudent(me);
        s.setArtifactUrl(req.artifactUrl());
        s.setSubmittedAt(Instant.now());
        s.setStatus(SubmissionStatus.SUBMITTED);
        s.setScore(null);
        s.setFeedback(null);
        s.setVersion(s.getId() == null ? 1 : s.getVersion() + 1);

        s = submissions.save(s);
        return toDto(s);
    }

    public Page<SubmissionResponse> mySubmissions(Authentication auth, Pageable pageable) {
        Student me = requireStudent(auth);
        return submissions.findByStudent_Id(me.getId(), pageable).map(this::toDto);
    }

    // ----- Instructor/Admin actions -----

    public Page<SubmissionResponse> listByCourse(Long courseId, Authentication auth, Pageable pageable) {
        User who = requireAuth(auth);
        if (!canAccessCourseSubmissions(who, courseId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        return submissions.findByAssignment_Course_Id(courseId, pageable).map(this::toDto);
    }

    @Transactional
    public SubmissionResponse grade(Long submissionId, GradeRequest req, Authentication auth) {
        User who = requireAuth(auth);
        Submission s = submissions.findById(submissionId).orElseThrow(() -> notFound("Submission"));
        if (!canAccessCourseSubmissions(who, s.getAssignment().getCourse().getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        int max = s.getAssignment().getMaxPoints();
        if (req.score() < 0 || req.score() > max) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Score must be between 0 and " + max);
        }
        s.setScore(req.score());
        s.setFeedback(req.feedback());
        s.setStatus(SubmissionStatus.GRADED);
        s = submissions.save(s);

        // Optional: trigger completion evaluation here (future iteration)
        return toDto(s);
    }

    @Transactional
    public SubmissionResponse requestResubmission(Long submissionId, ResubmitRequest req, Authentication auth) {
        User who = requireAuth(auth);
        Submission s = submissions.findById(submissionId).orElseThrow(() -> notFound("Submission"));
        if (!canAccessCourseSubmissions(who, s.getAssignment().getCourse().getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        s.setStatus(SubmissionStatus.RESUBMIT_REQUESTED);
        s.setFeedback(req.feedback());
        s = submissions.save(s);
        return toDto(s);
    }

    // ----- helpers -----

    private Student requireStudent(Authentication auth) {
        User u = requireAuth(auth);
        if (u.getRole() != Role.STUDENT || !(u instanceof Student s)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only students can submit");
        }
        return s;
    }

    private User requireAuth(Authentication auth) {
        if (auth == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return users.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    private boolean canAccessCourseSubmissions(User who, Long courseId) {
        return who.getRole() == Role.ADMIN ||
                (who.getRole() == Role.INSTRUCTOR &&
                        courses.existsByIdAndInstructor_Id(courseId, who.getId()));
    }

    private ResponseStatusException notFound(String what) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, what + " not found");
    }

    private SubmissionResponse toDto(Submission s) {
        return new SubmissionResponse(
                s.getId(),
                s.getAssignment().getId(),
                s.getAssignment().getCourse().getId(),
                s.getStudent().getId(),
                s.getSubmittedAt(),
                s.getArtifactUrl(),
                s.getStatus(),
                s.getScore(),
                s.getFeedback(),
                s.getVersion()
        );
    }
}
