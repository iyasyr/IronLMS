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
@DiscriminatorValue("INSTRUCTOR")
public class Instructor extends User {

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;
}
