package com.epam.gymcrm.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class Trainee {

    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private boolean active;
    private LocalDate dateOfBirth;
    private String address;
}
