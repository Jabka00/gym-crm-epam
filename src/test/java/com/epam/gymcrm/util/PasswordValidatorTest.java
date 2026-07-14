package com.epam.gymcrm.util;

import com.epam.gymcrm.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordValidatorTest {

    private final PasswordValidator passwordValidator = new PasswordValidator(
            "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{8,}$",
            "Password must be at least 8 characters and contain uppercase, lowercase, and digit");

    @Test
    void shouldRejectNullPassword() {
        assertThatThrownBy(() -> passwordValidator.validate(null))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Password cannot be null");
    }

    @Test
    void shouldRejectWeakPassword() {
        assertThatThrownBy(() -> passwordValidator.validate("weak"))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Password must be at least 8 characters and contain uppercase, lowercase, and digit");
    }

    @Test
    void shouldAcceptValidPassword() {
        assertThatCode(() -> passwordValidator.validate("ValidPass1"))
                .doesNotThrowAnyException();
    }
}
