package com.devops.paylite.service.dto;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PaymentCreateRequestDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldAcceptAccountWithExactlySixteenDigits() {
        var request = new PaymentCreateRequestDto("8600123412341234", 100L);

        assertTrue(validator.validate(request).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = { "123456789012345", "12345678901234567", "12345678901234AB" })
    void shouldRejectAccountUnlessItContainsExactlySixteenDigits(String account) {
        var request = new PaymentCreateRequestDto(account, 100L);

        assertFalse(validator.validate(request).isEmpty());
    }
}
