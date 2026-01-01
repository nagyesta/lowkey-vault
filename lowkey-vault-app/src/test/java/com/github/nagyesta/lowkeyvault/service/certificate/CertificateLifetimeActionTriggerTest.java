package com.github.nagyesta.lowkeyvault.service.certificate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.OffsetDateTime;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstants.NOW;
import static com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionTriggerType.DAYS_BEFORE_EXPIRY;
import static com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionTriggerType.LIFETIME_PERCENTAGE;

class CertificateLifetimeActionTriggerIntegrationTest {

    @SuppressWarnings("checkstyle:MagicNumber")
    public static Stream<Arguments> validValidationProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(LIFETIME_PERCENTAGE, 1, 1))
                .add(Arguments.of(LIFETIME_PERCENTAGE, 10, 99))
                .add(Arguments.of(LIFETIME_PERCENTAGE, 5, 42))
                .add(Arguments.of(DAYS_BEFORE_EXPIRY, 1, 27))
                .add(Arguments.of(DAYS_BEFORE_EXPIRY, 10, 270))
                .add(Arguments.of(DAYS_BEFORE_EXPIRY, 5, 1))
                .add(Arguments.of(DAYS_BEFORE_EXPIRY, 4, 42))
                .build();
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    public static Stream<Arguments> validTriggerProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(LIFETIME_PERCENTAGE, NOW, NOW.plusDays(100), 75, 75))
                .add(Arguments.of(LIFETIME_PERCENTAGE, NOW, NOW.plusDays(360), 50, 180))
                .add(Arguments.of(DAYS_BEFORE_EXPIRY, NOW, NOW.plusDays(360), 50, 310))
                .add(Arguments.of(DAYS_BEFORE_EXPIRY, NOW, NOW.plusDays(200), 42, 158))
                .build();
    }

    @ParameterizedTest
    @MethodSource("validValidationProvider")
    void testValidateShouldNotThrowExceptionsWhenCalledInValidState(
            final CertificateLifetimeActionTriggerType triggerType,
            final int validityMonths,
            final int value) {
        //given
        final var underTest = new CertificateLifetimeActionTrigger(triggerType, value);

        //when
        Assertions.assertDoesNotThrow(() -> underTest.validate(validityMonths));

        //then
    }

    @ParameterizedTest
    @MethodSource("validTriggerProvider")
    void testTriggersAfterDaysShouldReturnAccurateNumberOfDaysWhenCalledWithValidInput(
            final CertificateLifetimeActionTriggerType triggerType,
            final OffsetDateTime validityStart,
            final OffsetDateTime expiry,
            final int value,
            final int expectedValue) {
        //given
        final var underTest = new CertificateLifetimeActionTrigger(triggerType, value);

        //when
        final var actual = underTest.triggersAfterDays(validityStart, expiry);

        //then
        Assertions.assertEquals(expectedValue, actual);
    }

}
