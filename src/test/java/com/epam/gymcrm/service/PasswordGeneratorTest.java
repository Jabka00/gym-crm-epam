package com.epam.gymcrm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordGeneratorTest {

    private PasswordGenerator passwordGenerator;

    @BeforeEach
    void setUp() {
        passwordGenerator = new PasswordGenerator();
    }

    @Test
    void shouldGeneratePasswordWithTenCharacters() {
        String password = passwordGenerator.generatePassword();

        assertThat(password).hasSize(10);
    }
}
