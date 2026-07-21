package com.epam.gymcrm.service;

import com.epam.gymcrm.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

    @Test
    void shouldReuseInMemoryCounterWithoutHittingDbAgain() {
        when(userRepository.findUsernamesStartingWith("John.Smith")).thenReturn(List.of());

        assertThat(usernameGenerator.generateUniqueUsername("John", "Smith")).isEqualTo("John.Smith");
        assertThat(usernameGenerator.generateUniqueUsername("John", "Smith")).isEqualTo("John.Smith1");
        assertThat(usernameGenerator.generateUniqueUsername("John", "Smith")).isEqualTo("John.Smith2");

        verify(userRepository, times(1)).findUsernamesStartingWith("John.Smith");
    }

    @Test
    void shouldGenerateDistinctUsernamesUnderConcurrentAccess() throws InterruptedException {
        when(userRepository.findUsernamesStartingWith("John.Smith")).thenReturn(List.of());

        int threads = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        Set<String> generated = ConcurrentHashMap.newKeySet();

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    generated.add(usernameGenerator.generateUniqueUsername("John", "Smith"));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        assertThat(ready.await(2, TimeUnit.SECONDS)).isTrue();
        start.countDown();
        executor.shutdown();
        assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();

        assertThat(generated).hasSize(threads);
        assertThat(generated).contains("John.Smith");
        verify(userRepository, times(1)).findUsernamesStartingWith("John.Smith");
    }
}
