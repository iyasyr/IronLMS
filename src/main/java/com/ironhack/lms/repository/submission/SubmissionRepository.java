package com.ironhack.lms.repository.submission;

import com.ironhack.lms.domain.submission.Submission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    Page<Submission> findByStudent_Id(Long studentId, Pageable pageable);
    Page<Submission> findByAssignment_Course_Id(Long courseId, Pageable pageable);
    Optional<Submission> findByAssignment_IdAndStudent_Id(Long assignmentId, Long studentId);
}
