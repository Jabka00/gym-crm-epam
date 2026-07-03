package com.epam.gymcrm.service;

import com.epam.gymcrm.repository.TraineeRepository;
import com.epam.gymcrm.repository.TrainerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserCredentialServiceTest {

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private PasswordGenerator passwordGenerator;

    @InjectMocks
    private UserCredentialService userCredentialService;

    @Test
    void shouldGenerateBaseUsernameWhenUnique() {
        when(traineeRepository.existsByUsername("John.Smith")).thenReturn(false);
        when(trainerRepository.existsByUsername("John.Smith")).thenReturn(false);

        String username = userCredentialService.generateUniqueUsername("John", "Smith");

        assertThat(username).isEqualTo("John.Smith");
        verify(traineeRepository, times(1)).existsByUsername("John.Smith");
        verify(trainerRepository, times(1)).existsByUsername("John.Smith");
    }

    @Test
    void shouldAppendSerialNumberWhenUsernameExists() {
        when(traineeRepository.existsByUsername("John.Smith")).thenReturn(true);
        when(traineeRepository.existsByUsername("John.Smith1")).thenReturn(false);
        when(trainerRepository.existsByUsername("John.Smith1")).thenReturn(false);

        String username = userCredentialService.generateUniqueUsername("John", "Smith");

        assertThat(username).isEqualTo("John.Smith1");
        verify(traineeRepository, times(1)).existsByUsername("John.Smith");
        verify(traineeRepository, times(1)).existsByUsername("John.Smith1");
        verify(trainerRepository, times(1)).existsByUsername("John.Smith1");
        verify(trainerRepository, never()).existsByUsername("John.Smith");
    }

    @Test
    void shouldDelegatePasswordGeneration() {
        when(passwordGenerator.generatePassword()).thenReturn("abcdefghij");

        String password = userCredentialService.generatePassword();

        assertThat(password).isEqualTo("abcdefghij");
        verify(passwordGenerator, times(1)).generatePassword();
    }
}
