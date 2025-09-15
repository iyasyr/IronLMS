package com.ironhack.lms.domain.course;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter @Setter
@NoArgsConstructor
@Entity
@Table(name = "assignment")
public class Assignment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String instructions;

    @Column(name = "due_at")
    private Instant dueAt;

    @Column(name = "max_points", nullable = false)
    private int maxPoints;

    @Column(name = "allow_late", nullable = false)
    private boolean allowLate = false;
}
