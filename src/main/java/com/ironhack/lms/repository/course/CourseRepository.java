package com.ironhack.lms.repository.course;

import com.ironhack.lms.domain.course.Course;
import com.ironhack.lms.domain.course.CourseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
    Page<Course> findByStatus(CourseStatus status, Pageable pageable);
    Page<Course> findByInstructor_Id(Long instructorId, Pageable pageable);
    boolean existsByIdAndInstructor_Id(Long courseId, Long instructorId);
}
