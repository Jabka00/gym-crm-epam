package com.epam.gymcrm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CredentialGeneratorTest {

    private CredentialGenerator credentialGenerator;

    @BeforeEach
    void setUp() {
        credentialGenerator = new CredentialGenerator();
    }

    @Test
    void shouldGenerateUsernameWithoutSuffixWhenUnique() {
        String username = credentialGenerator.generateUsername("John", "Smith", Set.of());

        assertThat(username).isEqualTo("John.Smith");
    }

    @Test
    void shouldGenerateUsernameWithSuffixWhenDuplicateExists() {
        String username = credentialGenerator.generateUsername("John", "Smith", Set.of("John.Smith"));

        assertThat(username).isEqualTo("John.Smith1");
    }

    @Test
    void shouldGenerateUsernameWithIncrementedSuffixWhenMultipleDuplicatesExist() {
        String username = credentialGenerator.generateUsername(
                "John", "Smith", Set.of("John.Smith", "John.Smith1"));

        assertThat(username).isEqualTo("John.Smith2");
    }

    @Test
    void shouldGeneratePasswordWithTenCharacters() {
        String password = credentialGenerator.generatePassword();

        assertThat(password).hasSize(10);
    }
}
