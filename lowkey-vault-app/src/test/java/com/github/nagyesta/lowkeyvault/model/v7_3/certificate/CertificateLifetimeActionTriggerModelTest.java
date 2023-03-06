package com.github.nagyesta.lowkeyvault.model.v7_3.certificate;

import com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionTrigger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionTriggerType.DAYS_BEFORE_EXPIRY;
import static com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionTriggerType.LIFETIME_PERCENTAGE;

class CertificateLifetimeActionTriggerModelTest {

    @SuppressWarnings("checkstyle:MagicNumber")
    public static Stream<Arguments> invalidDayProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(28, 1))
                .add(Arguments.of(271, 5))
                .add(Arguments.of(271, 10))
                .build();
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    public static Stream<Arguments> validDayProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(1, 1))
                .add(Arguments.of(10, 1))
                .add(Arguments.of(27, 1))
                .add(Arguments.of(42, 5))
                .add(Arguments.of(270, 10))
                .build();
    }

    @Test
    void testValidateShouldThrowExceptionWhenBothFieldsAreNull() {
        //given
        final CertificateLifetimeActionTriggerModel underTest = new CertificateLifetimeActionTriggerModel();

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.validate(1));

        //then + exception
    }

    @Test
    void testValidateShouldNotCheckDaysBeforeExpiryWhenDaysBeforeExpiryIsNull() {
        //given
        final CertificateLifetimeActionTriggerModel underTest = new CertificateLifetimeActionTriggerModel();
        underTest.setLifetimePercentage(1);

        //when
        Assertions.assertDoesNotThrow(() -> underTest.validate(1));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("invalidDayProvider")
    void testValidateShouldThrowExceptionWhenDaysBeforeExpiryIsTooBig(final int days, final int validityMonths) {
        //given
        final CertificateLifetimeActionTriggerModel underTest = new CertificateLifetimeActionTriggerModel();
        underTest.setDaysBeforeExpiry(days);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.validate(validityMonths));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("validDayProvider")
    void testValidateShouldNotThrowExceptionWhenDaysBeforeExpiryIsValid(final int days, final int validityMonths) {
        //given
        final CertificateLifetimeActionTriggerModel underTest = new CertificateLifetimeActionTriggerModel();
        underTest.setDaysBeforeExpiry(days);

        //when
        Assertions.assertDoesNotThrow(() -> underTest.validate(validityMonths));

        //then + NO exception
    }

    @Test
    void testAsTriggerEntityShouldIdentifyTriggerTypeWhenDaysBeforeExpiryIsSet() {
        //given
        final CertificateLifetimeActionTriggerModel underTest = new CertificateLifetimeActionTriggerModel();
        final int expectedDays = 1;
        underTest.setDaysBeforeExpiry(expectedDays);

        //when
        final CertificateLifetimeActionTrigger actual = underTest.asTriggerEntity();

        //then
        Assertions.assertEquals(DAYS_BEFORE_EXPIRY, actual.getTriggerType());
        Assertions.assertEquals(expectedDays, actual.getValue());
    }

    @Test
    void testAsTriggerEntityShouldIdentifyTriggerTypeWhenLifetimePercentageIsSet() {
        //given
        final CertificateLifetimeActionTriggerModel underTest = new CertificateLifetimeActionTriggerModel();
        final int expectedDays = 1;
        underTest.setLifetimePercentage(expectedDays);

        //when
        final CertificateLifetimeActionTrigger actual = underTest.asTriggerEntity();

        //then
        Assertions.assertEquals(LIFETIME_PERCENTAGE, actual.getTriggerType());
        Assertions.assertEquals(expectedDays, actual.getValue());
    }
}
