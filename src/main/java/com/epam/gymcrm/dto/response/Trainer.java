package com.epam.gymcrm.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Trainer {

    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private boolean active;
    private TrainingType specialization;
}
