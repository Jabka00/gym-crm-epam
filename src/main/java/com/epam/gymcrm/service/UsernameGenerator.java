package com.epam.gymcrm.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class UsernameGenerator {

    private static final Pattern SUFFIX_PATTERN = Pattern.compile("^(.*?)(\\d+)$");

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
}
