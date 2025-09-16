package com.ironhack.lms.domain.enrollment;

import com.ironhack.lms.domain.course.Course;
import com.ironhack.lms.domain.user.Student;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter @Setter
@NoArgsConstructor
@Entity
@Table(name = "enrollment",
        uniqueConstraints = @UniqueConstraint(name = "uq_enroll", columnNames = {"student_id","course_id"}))
public class Enrollment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(name = "enrolled_at", nullable = false)
    private Instant enrolledAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EnrollmentStatus status = EnrollmentStatus.ACTIVE;

    @PrePersist
    void prePersist() {
        if (enrolledAt == null) enrolledAt = Instant.now();
        if (status == null) status = EnrollmentStatus.ACTIVE;
    }
}
