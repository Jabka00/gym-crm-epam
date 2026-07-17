package com.epam.gymcrm.service;

import com.epam.gymcrm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsernameGenerator {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public synchronized String generateUniqueUsername(String firstName, String lastName) {
        String baseUsername = firstName + "." + lastName;
        Set<String> existing = new HashSet<>(userRepository.findUsernamesStartingWith(baseUsername));

        if (!existing.contains(baseUsername)) {
            log.debug("Generated unique username");
            return baseUsername;
        }

        int serialNumber = 1;
        while (existing.contains(baseUsername + serialNumber)) {
            serialNumber++;
        }

        log.debug("Generated unique username");
        return baseUsername + serialNumber;
    }
}
