package com.github.nagyesta.lowkeyvault.model.v7_3.key.validator;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Period;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;

class ExpiryPeriodValidatorTest {

    @ExpiryPeriod
    private Period dummy;

    public static Stream<Arguments> isValidProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(Period.parse("-P1D"), false))
                .add(Arguments.of(Period.parse("P1D"), false))
                .add(Arguments.of(Period.parse("P5D"), false))
                .add(Arguments.of(Period.parse("P10D"), false))
                .add(Arguments.of(Period.parse("P26D"), false))
                .add(Arguments.of(Period.parse("P27D"), false))
                .add(Arguments.of(Period.parse("P28D"), true))
                .add(Arguments.of(Period.parse("P29D"), true))
                .add(Arguments.of(Period.parse("P1M"), true))
                .add(Arguments.of(Period.parse("P2M"), true))
                .add(Arguments.of(Period.parse("P1Y2M"), true))
                .add(Arguments.of(null, true))
                .build();
    }

    @ParameterizedTest
    @MethodSource("isValidProvider")
    void testIsValidShouldReturnTrueOnlyWhenCalledWithValidData(final Period input, final boolean expected) throws NoSuchFieldException {
        //given
        final ExpiryPeriodValidator underTest = new ExpiryPeriodValidator();
        final ExpiryPeriod annotation = this.getClass().getDeclaredField("dummy").getAnnotation(ExpiryPeriod.class);

        //when
        underTest.initialize(annotation);
        final boolean actual = underTest.isValid(input, mock(ConstraintValidatorContext.class));

        //then
        Assertions.assertEquals(expected, actual);
    }

}
