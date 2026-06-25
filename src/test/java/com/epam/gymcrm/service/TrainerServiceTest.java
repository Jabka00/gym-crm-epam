package com.epam.gymcrm.service;

import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.repository.TrainerRepository;
import com.epam.gymcrm.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private CredentialGenerator credentialGenerator;

    @InjectMocks
    private TrainerService trainerService;

    @Test
    void shouldCreateTrainerWithGeneratedCredentials() {
        Trainer trainer = TestDataFactory.createDefaultTrainer();
        when(trainerRepository.findAll()).thenReturn(List.of());
        when(credentialGenerator.generateUsername(eq("John"), eq("Smith"), eq(Set.of())))
                .thenReturn("John.Smith");
        when(credentialGenerator.generatePassword()).thenReturn("abcdefghij");
        when(trainerRepository.save(any(Trainer.class))).thenAnswer(invocation -> {
            Trainer saved = invocation.getArgument(0);
            saved.setUserId(1L);
            return saved;
        });

        Trainer created = trainerService.create(trainer);

        assertThat(created.getUserId()).isEqualTo(1L);
        assertThat(created.getUsername()).isEqualTo("John.Smith");
        assertThat(created.getPassword()).isEqualTo("abcdefghij");
        assertThat(created.isActive()).isTrue();
        verify(trainerRepository).save(trainer);
    }

    @Test
    void shouldUpdateTrainer() {
        Trainer trainer = TestDataFactory.createTrainerWithCredentials();
        trainer.setUserId(1L);
        when(trainerRepository.update(trainer)).thenReturn(trainer);

        Trainer updated = trainerService.update(trainer);

        assertThat(updated).isSameAs(trainer);
        verify(trainerRepository).update(trainer);
    }

    @Test
    void shouldFindTrainerById() {
        Trainer trainer = TestDataFactory.createTrainerWithCredentials();
        trainer.setUserId(1L);
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(trainer));

        assertThat(trainerService.findById(1L)).contains(trainer);
    }

    @Test
    void shouldFindAllTrainers() {
        Trainer trainer = TestDataFactory.createTrainerWithCredentials();
        when(trainerRepository.findAll()).thenReturn(List.of(trainer));

        assertThat(trainerService.findAll()).containsExactly(trainer);
    }
}
