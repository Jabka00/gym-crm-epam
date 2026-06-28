package com.epam.gymcrm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class CredentialGeneratorTest {

    private CredentialGenerator credentialGenerator;

    @BeforeEach
    void setUp() {
        credentialGenerator = new CredentialGenerator();
    }

    @Test
    void shouldGenerateUsernameWithoutSuffixWhenUnique() {
        var counters = new ConcurrentHashMap<String, AtomicInteger>();

        String username = credentialGenerator.generateUsername("John", "Smith", counters);

        assertThat(username).isEqualTo("John.Smith");
    }

    @Test
    void shouldGenerateUsernameWithSuffixWhenCalledTwice() {
        var counters = new ConcurrentHashMap<String, AtomicInteger>();
        credentialGenerator.generateUsername("John", "Smith", counters);

        String username = credentialGenerator.generateUsername("John", "Smith", counters);

        assertThat(username).isEqualTo("John.Smith1");
    }

    @Test
    void shouldGenerateUsernameWithIncrementedSuffixWhenCalledThreeTimes() {
        var counters = new ConcurrentHashMap<String, AtomicInteger>();
        credentialGenerator.generateUsername("John", "Smith", counters);
        credentialGenerator.generateUsername("John", "Smith", counters);

        String username = credentialGenerator.generateUsername("John", "Smith", counters);

        assertThat(username).isEqualTo("John.Smith2");
    }

    @Test
    void shouldGeneratePasswordWithTenCharacters() {
        String password = credentialGenerator.generatePassword();

        assertThat(password).hasSize(10);
    }
}
