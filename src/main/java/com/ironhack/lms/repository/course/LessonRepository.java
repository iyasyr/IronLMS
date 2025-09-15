package com.ironhack.lms.repository.course;

import com.ironhack.lms.domain.course.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByCourse_IdOrderByOrderIndexAsc(Long courseId);
}
