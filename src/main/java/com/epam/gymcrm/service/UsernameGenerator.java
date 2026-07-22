package com.epam.gymcrm.service;

import com.epam.gymcrm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsernameGenerator {

    private final UserRepository userRepository;
    private final ConcurrentHashMap<String, AtomicInteger> serialCounters = new ConcurrentHashMap<>();

    @Transactional(readOnly = true)
    public String generateUniqueUsername(String firstName, String lastName) {
        String baseUsername = firstName + "." + lastName;
        AtomicInteger counter = serialCounters.computeIfAbsent(baseUsername, this::initCounterFromDb);

        int serial = counter.getAndIncrement();
        String username = serial == 0 ? baseUsername : baseUsername + serial;
        log.debug("Generated unique username={}", username);
        return username;
    }

    private AtomicInteger initCounterFromDb(String baseUsername) {
        List<String> existing = userRepository.findUsernamesStartingWith(baseUsername);

        int maxSerial = existing.stream()
                .map(username -> username.substring(baseUsername.length()))
                .filter(suffix -> !suffix.isEmpty())
                .filter(suffix -> suffix.chars().allMatch(Character::isDigit))
                .mapToInt(Integer::parseInt)
                .max()
                .orElse(0);

        boolean baseTaken = existing.stream().anyMatch(baseUsername::equals);
        return new AtomicInteger(baseTaken ? maxSerial + 1 : 0);
    }
}
