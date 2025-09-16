package com.ironhack.lms.repository.enrollment;

import com.ironhack.lms.domain.enrollment.Enrollment;
import com.ironhack.lms.domain.enrollment.EnrollmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    Page<Enrollment> findByStudent_Id(Long studentId, Pageable pageable);
    Page<Enrollment> findByStudent_IdAndStatus(Long studentId, EnrollmentStatus status, Pageable pageable);
    boolean existsByCourse_IdAndStudent_Id(Long courseId, Long studentId);
    boolean existsByCourse_IdAndStudent_IdAndStatus(Long courseId, Long studentId, EnrollmentStatus status);
    Optional<Enrollment> findByIdAndStudent_Id(Long id, Long studentId);
}
