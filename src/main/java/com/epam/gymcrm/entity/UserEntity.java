package com.epam.gymcrm.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public abstract class UserEntity {
    private Long userId;
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private boolean active;
}
