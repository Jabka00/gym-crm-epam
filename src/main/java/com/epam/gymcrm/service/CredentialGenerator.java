package com.epam.gymcrm.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
public class CredentialGenerator {

    private static final String PASSWORD_ALPHABET = IntStream.concat(
                    IntStream.concat(
                            IntStream.rangeClosed('A', 'Z'),
                            IntStream.rangeClosed('a', 'z')),
                    IntStream.rangeClosed('0', '9'))
            .mapToObj(c -> String.valueOf((char) c))
            .collect(Collectors.joining());

    private static final int PASSWORD_LENGTH = 10;

    private static final Pattern SUFFIX_PATTERN = Pattern.compile("^(.*?)(\\d+)$");

    private final SecureRandom random = new SecureRandom();

    /**
     * Shared across all User-derived entities (trainees and trainers) so that the
     * username prefix is unique per User table, not per individual table.
     */
    private final ConcurrentHashMap<String, AtomicInteger> usernameCounters = new ConcurrentHashMap<>();

    public String generateUsername(String firstName, String lastName) {
        String base = firstName + "." + lastName;
        AtomicInteger counter = usernameCounters.computeIfAbsent(base, k -> new AtomicInteger(0));
        int count = counter.getAndIncrement();
        String username = count == 0
                ? base
                : new StringBuilder(base).append(count).toString();
        log.debug("Username generated");
        return username;
    }

    public void registerExistingUsername(String username) {
        Matcher m = SUFFIX_PATTERN.matcher(username);
        if (m.matches()) {
            int needed = Integer.parseInt(m.group(2)) + 1;
            usernameCounters.compute(m.group(1), (k, v) -> {
                if (v == null) return new AtomicInteger(needed);
                v.updateAndGet(cur -> Math.max(cur, needed));
                return v;
            });
        } else {
            usernameCounters.compute(username, (k, v) -> {
                if (v == null) return new AtomicInteger(1);
                v.updateAndGet(cur -> Math.max(cur, 1));
                return v;
            });
        }
    }

    public String generatePassword() {
        var password = new StringBuilder(PASSWORD_LENGTH);
        random.ints(PASSWORD_LENGTH, 0, PASSWORD_ALPHABET.length())
                .mapToObj(PASSWORD_ALPHABET::charAt)
                .forEach(password::append);
        return password.toString();
    }
}
