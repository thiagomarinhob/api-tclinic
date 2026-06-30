package com.jettech.api.solutions_clinic.model.usecase.tenant;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateConfirmationWindowBodyTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @ParameterizedTest
    @ValueSource(ints = {60, 120, 2880})
    void whenValueIsWithinBounds_thenNoViolations(int value) {
        UpdateConfirmationWindowBody body = new UpdateConfirmationWindowBody(value);

        Set<ConstraintViolation<UpdateConfirmationWindowBody>> violations = validator.validate(body);

        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {59, 0, -1, 2881})
    void whenValueIsOutOfBounds_thenViolated(int value) {
        UpdateConfirmationWindowBody body = new UpdateConfirmationWindowBody(value);

        Set<ConstraintViolation<UpdateConfirmationWindowBody>> violations = validator.validate(body);

        assertThat(violations).isNotEmpty();
    }

    @Test
    void whenValueIsNull_thenViolated() {
        UpdateConfirmationWindowBody body = new UpdateConfirmationWindowBody(null);

        Set<ConstraintViolation<UpdateConfirmationWindowBody>> violations = validator.validate(body);

        assertThat(violations).isNotEmpty();
    }
}
