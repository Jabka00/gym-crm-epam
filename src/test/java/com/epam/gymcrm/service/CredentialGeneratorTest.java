package com.epam.gymcrm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CredentialGeneratorTest {

    private CredentialGenerator credentialGenerator;

    @BeforeEach
    void setUp() {
        credentialGenerator = new CredentialGenerator();
    }

    @Test
    void shouldGenerateUsernameWithoutSuffixWhenUnique() {
        String username = credentialGenerator.generateUsername("John", "Smith");

        assertThat(username).isEqualTo("John.Smith");
    }

    @Test
    void shouldGenerateUsernameWithSuffixWhenCalledTwice() {
        credentialGenerator.generateUsername("John", "Smith");

        String username = credentialGenerator.generateUsername("John", "Smith");

        assertThat(username).isEqualTo("John.Smith1");
    }

    @Test
    void shouldGenerateUsernameWithIncrementedSuffixWhenCalledThreeTimes() {
        credentialGenerator.generateUsername("John", "Smith");
        credentialGenerator.generateUsername("John", "Smith");

        String username = credentialGenerator.generateUsername("John", "Smith");

        assertThat(username).isEqualTo("John.Smith2");
    }

    @Test
    void shouldShareUsernameUniquenessAcrossEntities() {
        credentialGenerator.registerExistingUsername("John.Smith");

        String username = credentialGenerator.generateUsername("John", "Smith");

        assertThat(username).isEqualTo("John.Smith1");
    }

    @Test
    void shouldGeneratePasswordWithTenCharacters() {
        String password = credentialGenerator.generatePassword();

        assertThat(password).hasSize(10);
    }
}
