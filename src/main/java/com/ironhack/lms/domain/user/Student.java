package com.ironhack.lms.domain.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter @Setter
@NoArgsConstructor
@SuperBuilder
@Entity
@DiscriminatorValue("STUDENT")
public class Student extends User {

    @Column(name = "student_number", length = 64)
    private String studentNumber;
}
