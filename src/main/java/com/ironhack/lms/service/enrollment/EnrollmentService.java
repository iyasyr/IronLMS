package com.ironhack.lms.service.enrollment;

import com.ironhack.lms.domain.course.Course;
import com.ironhack.lms.domain.course.CourseStatus;
import com.ironhack.lms.domain.enrollment.Enrollment;
import com.ironhack.lms.domain.enrollment.EnrollmentStatus;
import com.ironhack.lms.domain.user.Role;
import com.ironhack.lms.domain.user.Student;
import com.ironhack.lms.domain.user.User;
import com.ironhack.lms.repository.course.CourseRepository;
import com.ironhack.lms.repository.enrollment.EnrollmentRepository;
import com.ironhack.lms.repository.user.UserRepository;
import com.ironhack.lms.web.enrollment.dto.EnrollmentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollments;
    private final CourseRepository courses;
    private final UserRepository users;

    // --- Queries ---

    public Page<EnrollmentResponse> myEnrollments(Authentication auth, Pageable pageable) {
        Student me = requireStudent(auth);
        return enrollments.findByStudent_Id(me.getId(), pageable).map(this::toDto);
    }

    // --- Commands ---

    public EnrollmentResponse enroll(Long courseId, Authentication auth) {
        Student me = requireStudent(auth);
        Course c = courses.findById(courseId).orElseThrow(() -> notFound("Course"));

        if (c.getStatus() != CourseStatus.PUBLISHED) {
            // hide drafts: as a student you cannot enroll into non-published courses
            throw notFound("Course");
        }
        if (enrollments.existsByCourse_IdAndStudent_Id(courseId, me.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already enrolled");
        }

        Enrollment e = new Enrollment();
        e.setCourse(c);
        e.setStudent(me);
        e = enrollments.save(e);
        return toDto(e);
    }

    public EnrollmentResponse cancel(Long enrollmentId, Authentication auth) {
        Student me = requireStudent(auth);
        Enrollment e = enrollments.findByIdAndStudent_Id(enrollmentId, me.getId())
                .orElseThrow(() -> notFound("Enrollment"));
        e.setStatus(EnrollmentStatus.CANCELLED);
        return toDto(enrollments.save(e));
    }

    public EnrollmentResponse completeByStaff(Long enrollmentId, Authentication auth) {
        var staff = users.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        var e = enrollments.findById(enrollmentId)
                .orElseThrow(() -> notFound("Enrollment"));

        boolean isAdmin = staff.getRole() == Role.ADMIN;
        boolean isOwnerInstructor = staff.getRole() == Role.INSTRUCTOR
                && e.getCourse().getInstructor().getId().equals(staff.getId());

        if (!isAdmin && !isOwnerInstructor) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        e.setStatus(EnrollmentStatus.COMPLETED);
        return toDto(enrollments.save(e));
    }

    // --- helpers ---

    private Student requireStudent(Authentication auth) {
        if (auth == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        User u = users.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        if (u.getRole() != Role.STUDENT || !(u instanceof Student s)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only students can enroll");
        }
        return s;
    }

    private ResponseStatusException notFound(String what) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, what + " not found");
    }

    private EnrollmentResponse toDto(Enrollment e) {
        return new EnrollmentResponse(
                e.getId(),
                e.getCourse().getId(),
                e.getCourse().getTitle(),
                e.getStatus(),
                e.getEnrolledAt()
        );
    }
}
