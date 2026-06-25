package com.epam.gymcrm.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Set;

@Slf4j
@Service
public class CredentialGenerator {

    private static final String PASSWORD_ALPHABET =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int PASSWORD_LENGTH = 10;

    private final SecureRandom random = new SecureRandom();

    public String generateUsername(String firstName, String lastName, Set<String> existingUsernames) {
        String base = firstName + "." + lastName;
        String username = base;
        int suffix = 1;
        while (existingUsernames.contains(username)) {
            username = base + suffix++;
        }
        log.debug("Generated username: {}", username);
        return username;
    }

    public String generatePassword() {
        var password = new StringBuilder(PASSWORD_LENGTH);
        random.ints(PASSWORD_LENGTH, 0, PASSWORD_ALPHABET.length())
                .mapToObj(PASSWORD_ALPHABET::charAt)
                .forEach(password::append);
        log.debug("Generated random password");
        return password.toString();
    }
}
