package com.epam.gymcrm.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class TraineeEntity extends UserEntity {
    private LocalDate dateOfBirth;
    private String address;
}
