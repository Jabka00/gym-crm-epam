package com.epam.gymcrm.service;

import com.epam.gymcrm.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsernameGeneratorTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UsernameGenerator usernameGenerator;

    @Test
    void shouldGenerateBaseUsernameWhenUnique() {
        when(userRepository.findUsernamesStartingWith("John.Smith")).thenReturn(List.of());

        String username = usernameGenerator.generateUniqueUsername("John", "Smith");

        assertThat(username).isEqualTo("John.Smith");
        verify(userRepository, times(1)).findUsernamesStartingWith("John.Smith");
    }

    @Test
    void shouldAppendSerialNumberWhenUsernameExists() {
        when(userRepository.findUsernamesStartingWith("John.Smith"))
                .thenReturn(List.of("John.Smith"));

        String username = usernameGenerator.generateUniqueUsername("John", "Smith");

        assertThat(username).isEqualTo("John.Smith1");
        verify(userRepository, times(1)).findUsernamesStartingWith("John.Smith");
    }

    @Test
    void shouldSkipTakenSerialNumbers() {
        when(userRepository.findUsernamesStartingWith("John.Smith"))
                .thenReturn(List.of("John.Smith", "John.Smith1", "John.Smith2"));

        String username = usernameGenerator.generateUniqueUsername("John", "Smith");

        assertThat(username).isEqualTo("John.Smith3");
        verify(userRepository, times(1)).findUsernamesStartingWith("John.Smith");
    }
}
