package com.epam.gymcrm.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
public class PasswordGenerator {

    private static final String PASSWORD_ALPHABET = IntStream.concat(
                    IntStream.concat(
                            IntStream.rangeClosed('A', 'Z'),
                            IntStream.rangeClosed('a', 'z')),
                    IntStream.rangeClosed('0', '9'))
            .mapToObj(c -> String.valueOf((char) c))
            .collect(Collectors.joining());

    private static final int PASSWORD_LENGTH = 10;

    private final SecureRandom random = new SecureRandom();

    public String generatePassword() {
        var password = new StringBuilder(PASSWORD_LENGTH);
        random.ints(PASSWORD_LENGTH, 0, PASSWORD_ALPHABET.length())
                .mapToObj(PASSWORD_ALPHABET::charAt)
                .forEach(password::append);
        return password.toString();
    }
}
