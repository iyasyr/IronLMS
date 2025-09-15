package com.ironhack.lms.repository;


import com.ironhack.lms.domain.user.Instructor;
import com.ironhack.lms.domain.user.Role;
import com.ironhack.lms.domain.user.Student;
import com.ironhack.lms.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTests {

    @Autowired
    UserRepository repo;

    @Test
    void saveAndFind_studentAndInstructor() {
        Student s = Student.builder()
                .email("student@lms.local")
                .passwordHash("password")  // we'll hash later
                .fullName("Stu Dent")
                .role(Role.STUDENT)
                .build();
        repo.save(s);

        Instructor i = Instructor.builder()
                .email("instructor@lms.local")
                .passwordHash("password")
                .fullName("In Structor")
                .role(Role.INSTRUCTOR)
                .build();
        repo.save(i);

        assertThat(repo.findByEmail("student@lms.local")).isPresent();
        assertThat(repo.findByEmail("instructor@lms.local")).isPresent();
        assertThat(repo.findByRole(Role.STUDENT)).hasSize(1);
        assertThat(repo.findByRole(Role.INSTRUCTOR)).hasSize(1);
    }
}
