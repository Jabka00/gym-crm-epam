package com.epam.gymcrm.service;

import com.epam.gymcrm.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserCredentialServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserCredentialService userCredentialService;

    @Test
    void shouldGenerateBaseUsernameWhenUnique() {
        when(userRepository.existsByUsername("John.Smith")).thenReturn(false);

        String username = userCredentialService.generateUniqueUsername("John", "Smith");

        assertThat(username).isEqualTo("John.Smith");
        verify(userRepository, times(1)).existsByUsername("John.Smith");
    }

    @Test
    void shouldAppendSerialNumberWhenUsernameExists() {
        when(userRepository.existsByUsername("John.Smith")).thenReturn(true);
        when(userRepository.existsByUsername("John.Smith1")).thenReturn(false);

        String username = userCredentialService.generateUniqueUsername("John", "Smith");

        assertThat(username).isEqualTo("John.Smith1");
        verify(userRepository, times(1)).existsByUsername("John.Smith");
        verify(userRepository, times(1)).existsByUsername("John.Smith1");
    }
}
