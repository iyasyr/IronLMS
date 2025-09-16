package com.ironhack.lms.domain.submission;

import com.ironhack.lms.domain.course.Assignment;
import com.ironhack.lms.domain.user.Student;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter @Setter
@NoArgsConstructor
@Entity
@Table(name = "submission",
        uniqueConstraints = @UniqueConstraint(name = "uq_submission_one_per_student",
                columnNames = {"assignment_id","student_id"}))
public class Submission {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignment_id")
    private Assignment assignment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id")
    private Student student;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @Column(name = "artifact_url", nullable = false, length = 2048)
    private String artifactUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SubmissionStatus status = SubmissionStatus.SUBMITTED;

    @Column private Integer score;     // 0..maxPoints
    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Column(nullable = false)
    private int version = 1;

    @PrePersist
    void prePersist() {
        if (submittedAt == null) submittedAt = Instant.now();
        if (status == null) status = SubmissionStatus.SUBMITTED;
        if (version <= 0) version = 1;
    }
}
