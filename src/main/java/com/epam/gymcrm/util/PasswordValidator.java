package com.epam.gymcrm.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Slf4j
@Component
public class PasswordValidator {

    private final Pattern passwordPattern;
    private final String validationMessage;

    public PasswordValidator(
            @Value("${password.validation.pattern:^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{8,}$}") String pattern,
            @Value("${password.validation.message:Password must be at least 8 characters and contain uppercase, lowercase, and digit}") String message) {
        this.passwordPattern = Pattern.compile(pattern);
        this.validationMessage = message;
    }

    public void validate(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        if (!passwordPattern.matcher(password).matches()) {
            throw new IllegalArgumentException(validationMessage);
        }
        log.debug("Password validation successful");
    }
}
