package com.epam.gymcrm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UsernameGeneratorTest {

    private UsernameGenerator usernameGenerator;

    @BeforeEach
    void setUp() {
        usernameGenerator = new UsernameGenerator();
    }

    @Test
    void shouldGenerateUsernameWithoutSuffixWhenUnique() {
        String username = usernameGenerator.generateUsername("John", "Smith");

        assertThat(username).isEqualTo("John.Smith");
    }

    @Test
    void shouldGenerateUsernameWithSuffixWhenCalledTwice() {
        usernameGenerator.generateUsername("John", "Smith");

        String username = usernameGenerator.generateUsername("John", "Smith");

        assertThat(username).isEqualTo("John.Smith1");
    }

    @Test
    void shouldGenerateUsernameWithIncrementedSuffixWhenCalledThreeTimes() {
        usernameGenerator.generateUsername("John", "Smith");
        usernameGenerator.generateUsername("John", "Smith");

        String username = usernameGenerator.generateUsername("John", "Smith");

        assertThat(username).isEqualTo("John.Smith2");
    }

    @Test
    void shouldShareUsernameUniquenessAcrossEntities() {
        usernameGenerator.registerExistingUsername("John.Smith");

        String username = usernameGenerator.generateUsername("John", "Smith");

        assertThat(username).isEqualTo("John.Smith1");
    }
}
