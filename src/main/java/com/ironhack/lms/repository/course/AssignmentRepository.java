package com.ironhack.lms.repository.course;

import com.ironhack.lms.domain.course.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByCourse_Id(Long courseId);
}
