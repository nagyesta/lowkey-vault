package com.github.nagyesta.lowkeyvault.service.key.constants;

import com.github.nagyesta.lowkeyvault.TestConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.OffsetDateTime;
import java.time.Period;
import java.util.Optional;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.service.key.constants.LifetimeActionTriggerType.TIME_AFTER_CREATE;
import static com.github.nagyesta.lowkeyvault.service.key.constants.LifetimeActionTriggerType.TIME_BEFORE_EXPIRY;

class LifetimeActionTriggerTypeTest {

    @SuppressWarnings("checkstyle:MagicNumber")
    public static Stream<Arguments> validationDataProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(TIME_BEFORE_EXPIRY, null, 30, 7, false))
                .add(Arguments.of(TIME_BEFORE_EXPIRY, TestConstants.TIME_10_MINUTES_AGO, 30, 7, true))
                .add(Arguments.of(TIME_BEFORE_EXPIRY, TestConstants.NOW, 30, 5, false))
                .add(Arguments.of(TIME_BEFORE_EXPIRY, TestConstants.NOW, 30, 6, false))
                .add(Arguments.of(TIME_BEFORE_EXPIRY, TestConstants.NOW, 30, 8, true))
                .add(Arguments.of(TIME_BEFORE_EXPIRY, TestConstants.NOW, 1, 7, false))
                .add(Arguments.of(TIME_BEFORE_EXPIRY, TestConstants.NOW, 27, 7, false))
                .add(Arguments.of(TIME_BEFORE_EXPIRY, TestConstants.NOW, 28, 7, true))
                .add(Arguments.of(TIME_BEFORE_EXPIRY, TestConstants.NOW, null, 7, false))
                .add(Arguments.of(TIME_BEFORE_EXPIRY, TestConstants.NOW, 28, null, false))
                .add(Arguments.of(TIME_AFTER_CREATE, null, 30, 7, true))
                .add(Arguments.of(TIME_AFTER_CREATE, TestConstants.TIME_10_MINUTES_AGO, 30, 23, true))
                .add(Arguments.of(TIME_AFTER_CREATE, null, 30, 25, false))
                .add(Arguments.of(TIME_AFTER_CREATE, null, 30, 24, false))
                .add(Arguments.of(TIME_AFTER_CREATE, null, 30, 22, true))
                .add(Arguments.of(TIME_AFTER_CREATE, TestConstants.NOW, 1, 20, false))
                .add(Arguments.of(TIME_AFTER_CREATE, TestConstants.NOW, 27, 20, false))
                .add(Arguments.of(TIME_AFTER_CREATE, TestConstants.NOW, 28, 20, true))
                .add(Arguments.of(TIME_AFTER_CREATE, TestConstants.NOW, 28, 21, true))
                .add(Arguments.of(TIME_AFTER_CREATE, TestConstants.NOW, 28, 22, false))
                .add(Arguments.of(TIME_AFTER_CREATE, TestConstants.NOW, null, 21, false))
                .add(Arguments.of(TIME_AFTER_CREATE, TestConstants.NOW, 28, null, false))
                .build();
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    public static Stream<Arguments> shouldTriggerValidDataProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(TIME_BEFORE_EXPIRY, 50, 44, 7, true))
                .add(Arguments.of(TIME_BEFORE_EXPIRY, 50, 43, 7, true))
                .add(Arguments.of(TIME_BEFORE_EXPIRY, 50, 42, 7, false))
                .add(Arguments.of(TIME_BEFORE_EXPIRY, 50, 41, 7, false))
                .add(Arguments.of(TIME_BEFORE_EXPIRY, 28, 22, 7, true))
                .add(Arguments.of(TIME_BEFORE_EXPIRY, 28, 21, 7, true))
                .add(Arguments.of(TIME_BEFORE_EXPIRY, 28, 20, 7, false))
                .add(Arguments.of(TIME_BEFORE_EXPIRY, 10, 3, 7, true))
                .add(Arguments.of(TIME_BEFORE_EXPIRY, 10, 3, 6, false))
                .add(Arguments.of(TIME_AFTER_CREATE, 50, 44, 43, true))
                .add(Arguments.of(TIME_AFTER_CREATE, 50, 43, 43, true))
                .add(Arguments.of(TIME_AFTER_CREATE, 50, 42, 43, false))
                .add(Arguments.of(TIME_AFTER_CREATE, 50, 41, 43, false))
                .add(Arguments.of(TIME_AFTER_CREATE, 28, 22, 21, true))
                .add(Arguments.of(TIME_AFTER_CREATE, 28, 21, 21, true))
                .add(Arguments.of(TIME_AFTER_CREATE, 28, 20, 21, false))
                .add(Arguments.of(TIME_AFTER_CREATE, 10, 3, 3, true))
                .add(Arguments.of(TIME_AFTER_CREATE, 10, 3, 4, false))
                .build();
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    public static Stream<Arguments> shouldTriggerInvalidDataProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(TIME_BEFORE_EXPIRY, null, null, null))
                .add(Arguments.of(TIME_BEFORE_EXPIRY, TestConstants.TIME_10_MINUTES_AGO, null, 0))
                .add(Arguments.of(TIME_BEFORE_EXPIRY, TestConstants.TIME_10_MINUTES_AGO, TestConstants.NOW, null))
                .add(Arguments.of(TIME_AFTER_CREATE, null, null, null))
                .add(Arguments.of(TIME_AFTER_CREATE, TestConstants.TIME_10_MINUTES_AGO, null, null))
                .add(Arguments.of(TIME_AFTER_CREATE, null, TestConstants.NOW, 0))
                .build();
    }

    @ParameterizedTest
    @MethodSource("validationDataProvider")
    void testValidateShouldThrowExceptionWhenInputIsInvalid(
            final LifetimeActionTriggerType underTest, final OffsetDateTime expires,
            final Integer expiryDays, final Integer triggerDays, final boolean valid) {
        //given
        final var expiryPeriod = Optional.ofNullable(expiryDays).map(Period::ofDays).orElse(null);
        final var triggerPeriod = Optional.ofNullable(triggerDays).map(Period::ofDays).orElse(null);

        //when
        if (valid) {
            Assertions.assertDoesNotThrow(() -> underTest.validate(expires, expiryPeriod, triggerPeriod));
        } else {
            Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.validate(expires, expiryPeriod, triggerPeriod));
        }

        //then + exception if not valid
    }

    @ParameterizedTest
    @MethodSource("shouldTriggerValidDataProvider")
    void testShouldTriggerShouldReturnTrueWhenCalledInsideTheTriggerPeriod(
            final LifetimeActionTriggerType underTest, final int expiryAfterCreateDays,
            final int createOffsetDays, final int triggerDays, final boolean expected) {
        //given
        final var createTime = TestConstants.NOW.minusDays(createOffsetDays);
        final var expiryTime = createTime.plusDays(expiryAfterCreateDays);
        final var triggerPeriod = Period.ofDays(triggerDays);

        //when
        final var actual = underTest.shouldTrigger(createTime, expiryTime, triggerPeriod);

        //then
        Assertions.assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("shouldTriggerInvalidDataProvider")
    void testShouldTriggerShouldThrowExceptionWhenCalledWithInvalidData(
            final LifetimeActionTriggerType underTest, final OffsetDateTime createTime,
            final OffsetDateTime expiryTime, final Integer triggerDays) {
        //given
        final var triggerPeriod = Optional.ofNullable(triggerDays).map(Period::ofDays).orElse(null);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.shouldTrigger(createTime, expiryTime, triggerPeriod));

        //then + exception
    }
}
