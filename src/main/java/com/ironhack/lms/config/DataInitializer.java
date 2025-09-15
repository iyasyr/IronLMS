package com.ironhack.lms.config;

import com.ironhack.lms.domain.user.Instructor;
import com.ironhack.lms.domain.user.Role;
import com.ironhack.lms.domain.user.Student;
import com.ironhack.lms.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final PasswordEncoder encoder;

    @Bean
    CommandLineRunner initUsers(UserRepository repo) {
        return args -> {
            if (repo.count() == 0) {
                var s = Student.builder()
                        .email("student@lms.local")
                        .passwordHash(encoder.encode("password"))
                        .fullName("Stu Dent")
                        .role(Role.STUDENT)
                        .studentNumber("S-001")
                        .build();
                var i = Instructor.builder()
                        .email("instructor@lms.local")
                        .passwordHash(encoder.encode("password"))
                        .fullName("In Structor")
                        .role(Role.INSTRUCTOR)
                        .bio("Teaches Java")
                        .build();
                repo.save(s);
                repo.save(i);
            }
        };
    }
}
