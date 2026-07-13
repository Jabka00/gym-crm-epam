package com.epam.gymcrm.service;

import com.epam.gymcrm.repository.TraineeRepository;
import com.epam.gymcrm.repository.TrainerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCredentialService {

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;

    public synchronized String generateUniqueUsername(String firstName, String lastName) {
        String baseUsername = firstName + "." + lastName;
        String username = baseUsername;
        int serialNumber = 1;

        while (traineeRepository.existsByUsername(username) || trainerRepository.existsByUsername(username)) {
            username = baseUsername + serialNumber;
            serialNumber++;
        }

        log.debug("Generated unique username for base={}", baseUsername);
        return username;
    }
}
