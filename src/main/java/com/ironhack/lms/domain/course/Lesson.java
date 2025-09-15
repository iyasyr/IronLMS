package com.ironhack.lms.domain.course;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@Entity
@Table(name = "lesson")
public class Lesson {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "content_url", length = 2048)
    private String contentUrl;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;
}
