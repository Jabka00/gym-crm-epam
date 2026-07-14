package com.epam.gymcrm.util;

import com.epam.gymcrm.exception.ValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class DtoValidator {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    public <T> void validate(T dto) {
        if (dto == null) {
            throw new ValidationException("DTO cannot be null");
        }

        Set<ConstraintViolation<T>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new ValidationException(
                    violations.stream()
                            .map(ConstraintViolation::getMessage)
                            .collect(Collectors.joining(", "))
            );
        }
    }

    public <T> void validateForUpdate(T dto, Function<T, Long> idGetter, String entityName) {
        validate(dto);
        if (idGetter.apply(dto) == null) {
            throw new ValidationException(entityName + " id is required");
        }
    }
}
