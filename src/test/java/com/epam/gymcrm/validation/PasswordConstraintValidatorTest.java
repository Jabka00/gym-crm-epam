package com.epam.gymcrm.validation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordConstraintValidatorTest {

    private final PasswordConstraintValidator validator = new PasswordConstraintValidator();

    @Test
    void shouldRejectNullPassword() {
        assertThat(validator.isValid(null, null)).isFalse();
    }

    @Test
    void shouldRejectTooShortPassword() {
        assertThat(validator.isValid("Ab1cd", null)).isFalse();
    }

    @Test
    void shouldRejectPasswordWithoutUppercase() {
        assertThat(validator.isValid("lowercase1", null)).isFalse();
    }

    @Test
    void shouldRejectPasswordWithoutLowercase() {
        assertThat(validator.isValid("UPPERCASE1", null)).isFalse();
    }

    @Test
    void shouldRejectPasswordWithoutDigit() {
        assertThat(validator.isValid("NoDigitsHere", null)).isFalse();
    }

    @Test
    void shouldAcceptValidPassword() {
        assertThat(validator.isValid("ValidPass1", null)).isTrue();
    }
}
