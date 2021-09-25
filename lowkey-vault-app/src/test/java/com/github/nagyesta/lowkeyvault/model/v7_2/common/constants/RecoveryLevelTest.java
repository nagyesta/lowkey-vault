package com.github.nagyesta.lowkeyvault.model.v7_2.common.constants;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

class RecoveryLevelTest {

    public static Stream<Arguments> valueProvider() {
        final List<RecoveryLevel> list = new ArrayList<>();
        list.add(null);
        list.addAll(Arrays.asList(RecoveryLevel.values()));
        return list.stream()
                .map(value -> Arguments.of(
                        Optional.ofNullable(value).map(RecoveryLevel::getValue).orElse(null),
                        Optional.ofNullable(value).orElse(RecoveryLevel.PURGEABLE)));
    }

    public static Stream<Arguments> isRecoverableProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(RecoveryLevel.RECOVERABLE, true))
                .add(Arguments.of(RecoveryLevel.RECOVERABLE_AND_PURGEABLE, true))
                .add(Arguments.of(RecoveryLevel.RECOVERABLE_AND_PROTECTED_SUBSCRIPTION, true))
                .add(Arguments.of(RecoveryLevel.CUSTOMIZED_RECOVERABLE, true))
                .add(Arguments.of(RecoveryLevel.CUSTOMIZED_RECOVERABLE_AND_PURGEABLE, true))
                .add(Arguments.of(RecoveryLevel.CUSTOMIZED_RECOVERABLE_AND_PROTECTED_SUBSCRIPTION, true))
                .add(Arguments.of(RecoveryLevel.PURGEABLE, false))
                .build();
    }

    public static Stream<Arguments> isCustomizedProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(RecoveryLevel.RECOVERABLE, false))
                .add(Arguments.of(RecoveryLevel.RECOVERABLE_AND_PURGEABLE, false))
                .add(Arguments.of(RecoveryLevel.RECOVERABLE_AND_PROTECTED_SUBSCRIPTION, false))
                .add(Arguments.of(RecoveryLevel.CUSTOMIZED_RECOVERABLE, true))
                .add(Arguments.of(RecoveryLevel.CUSTOMIZED_RECOVERABLE_AND_PURGEABLE, true))
                .add(Arguments.of(RecoveryLevel.CUSTOMIZED_RECOVERABLE_AND_PROTECTED_SUBSCRIPTION, true))
                .add(Arguments.of(RecoveryLevel.PURGEABLE, false))
                .build();
    }

    public static Stream<Arguments> isPurgeableProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(RecoveryLevel.RECOVERABLE, false))
                .add(Arguments.of(RecoveryLevel.RECOVERABLE_AND_PURGEABLE, true))
                .add(Arguments.of(RecoveryLevel.RECOVERABLE_AND_PROTECTED_SUBSCRIPTION, false))
                .add(Arguments.of(RecoveryLevel.CUSTOMIZED_RECOVERABLE, false))
                .add(Arguments.of(RecoveryLevel.CUSTOMIZED_RECOVERABLE_AND_PURGEABLE, true))
                .add(Arguments.of(RecoveryLevel.CUSTOMIZED_RECOVERABLE_AND_PROTECTED_SUBSCRIPTION, false))
                .add(Arguments.of(RecoveryLevel.PURGEABLE, true))
                .build();
    }

    public static Stream<Arguments> isSubscriptionProtectedProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(RecoveryLevel.RECOVERABLE, false))
                .add(Arguments.of(RecoveryLevel.RECOVERABLE_AND_PURGEABLE, false))
                .add(Arguments.of(RecoveryLevel.RECOVERABLE_AND_PROTECTED_SUBSCRIPTION, true))
                .add(Arguments.of(RecoveryLevel.CUSTOMIZED_RECOVERABLE, false))
                .add(Arguments.of(RecoveryLevel.CUSTOMIZED_RECOVERABLE_AND_PURGEABLE, false))
                .add(Arguments.of(RecoveryLevel.CUSTOMIZED_RECOVERABLE_AND_PROTECTED_SUBSCRIPTION, true))
                .add(Arguments.of(RecoveryLevel.PURGEABLE, false))
                .build();
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    public static Stream<Arguments> checkValidityDaysProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(RecoveryLevel.PURGEABLE, null, true))
                .add(Arguments.of(RecoveryLevel.PURGEABLE, 0, false))
                .add(Arguments.of(RecoveryLevel.PURGEABLE, 6, false))
                .add(Arguments.of(RecoveryLevel.PURGEABLE, 7, false))
                .add(Arguments.of(RecoveryLevel.PURGEABLE, 8, false))
                .add(Arguments.of(RecoveryLevel.PURGEABLE, 90, false))
                .add(Arguments.of(RecoveryLevel.PURGEABLE, 91, false))
                .add(Arguments.of(RecoveryLevel.RECOVERABLE, null, false))
                .add(Arguments.of(RecoveryLevel.RECOVERABLE, 0, false))
                .add(Arguments.of(RecoveryLevel.RECOVERABLE, 6, false))
                .add(Arguments.of(RecoveryLevel.RECOVERABLE, 7, false))
                .add(Arguments.of(RecoveryLevel.RECOVERABLE, 8, false))
                .add(Arguments.of(RecoveryLevel.RECOVERABLE, 90, true))
                .add(Arguments.of(RecoveryLevel.RECOVERABLE, 91, false))
                .add(Arguments.of(RecoveryLevel.RECOVERABLE_AND_PURGEABLE, null, false))
                .add(Arguments.of(RecoveryLevel.RECOVERABLE_AND_PURGEABLE, 0, false))
                .add(Arguments.of(RecoveryLevel.RECOVERABLE_AND_PURGEABLE, 6, false))
                .add(Arguments.of(RecoveryLevel.RECOVERABLE_AND_PURGEABLE, 7, false))
                .add(Arguments.of(RecoveryLevel.RECOVERABLE_AND_PURGEABLE, 8, false))
                .add(Arguments.of(RecoveryLevel.RECOVERABLE_AND_PURGEABLE, 90, true))
                .add(Arguments.of(RecoveryLevel.RECOVERABLE_AND_PURGEABLE, 91, false))
                .add(Arguments.of(RecoveryLevel.RECOVERABLE_AND_PROTECTED_SUBSCRIPTION, null, false))
                .add(Arguments.of(RecoveryLevel.RECOVERABLE_AND_PROTECTED_SUBSCRIPTION, 0, false))
                .add(Arguments.of(RecoveryLevel.RECOVERABLE_AND_PROTECTED_SUBSCRIPTION, 6, false))
                .add(Arguments.of(RecoveryLevel.RECOVERABLE_AND_PROTECTED_SUBSCRIPTION, 7, false))
                .add(Arguments.of(RecoveryLevel.RECOVERABLE_AND_PROTECTED_SUBSCRIPTION, 8, false))
                .add(Arguments.of(RecoveryLevel.RECOVERABLE_AND_PROTECTED_SUBSCRIPTION, 90, true))
                .add(Arguments.of(RecoveryLevel.RECOVERABLE_AND_PROTECTED_SUBSCRIPTION, 91, false))
                .add(Arguments.of(RecoveryLevel.CUSTOMIZED_RECOVERABLE, null, false))
                .add(Arguments.of(RecoveryLevel.CUSTOMIZED_RECOVERABLE, 0, false))
                .add(Arguments.of(RecoveryLevel.CUSTOMIZED_RECOVERABLE, 6, false))
                .add(Arguments.of(RecoveryLevel.CUSTOMIZED_RECOVERABLE, 7, true))
                .add(Arguments.of(RecoveryLevel.CUSTOMIZED_RECOVERABLE, 8, true))
                .add(Arguments.of(RecoveryLevel.CUSTOMIZED_RECOVERABLE, 90, true))
                .add(Arguments.of(RecoveryLevel.CUSTOMIZED_RECOVERABLE, 91, false))
                .add(Arguments.of(RecoveryLevel.CUSTOMIZED_RECOVERABLE_AND_PURGEABLE, null, false))
                .add(Arguments.of(RecoveryLevel.CUSTOMIZED_RECOVERABLE_AND_PURGEABLE, 0, false))
                .add(Arguments.of(RecoveryLevel.CUSTOMIZED_RECOVERABLE_AND_PURGEABLE, 6, false))
                .add(Arguments.of(RecoveryLevel.CUSTOMIZED_RECOVERABLE_AND_PURGEABLE, 7, true))
                .add(Arguments.of(RecoveryLevel.CUSTOMIZED_RECOVERABLE_AND_PURGEABLE, 8, true))
                .add(Arguments.of(RecoveryLevel.CUSTOMIZED_RECOVERABLE_AND_PURGEABLE, 90, true))
                .add(Arguments.of(RecoveryLevel.CUSTOMIZED_RECOVERABLE_AND_PURGEABLE, 91, false))
                .add(Arguments.of(RecoveryLevel.CUSTOMIZED_RECOVERABLE_AND_PROTECTED_SUBSCRIPTION, null, false))
                .add(Arguments.of(RecoveryLevel.CUSTOMIZED_RECOVERABLE_AND_PROTECTED_SUBSCRIPTION, 0, false))
                .add(Arguments.of(RecoveryLevel.CUSTOMIZED_RECOVERABLE_AND_PROTECTED_SUBSCRIPTION, 6, false))
                .add(Arguments.of(RecoveryLevel.CUSTOMIZED_RECOVERABLE_AND_PROTECTED_SUBSCRIPTION, 7, true))
                .add(Arguments.of(RecoveryLevel.CUSTOMIZED_RECOVERABLE_AND_PROTECTED_SUBSCRIPTION, 8, true))
                .add(Arguments.of(RecoveryLevel.CUSTOMIZED_RECOVERABLE_AND_PROTECTED_SUBSCRIPTION, 90, true))
                .add(Arguments.of(RecoveryLevel.CUSTOMIZED_RECOVERABLE_AND_PROTECTED_SUBSCRIPTION, 91, false))
                .build();
    }

    @ParameterizedTest
    @MethodSource("valueProvider")
    void testForValueShouldReturnEnumWhenValueStringMatches(final String input, final RecoveryLevel expected) {
        //given

        //when
        final RecoveryLevel actual = RecoveryLevel.forValue(input);

        //then
        Assertions.assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("isRecoverableProvider")
    void testIsRecoverableShouldReturnValidValueWhenCalled(final RecoveryLevel underTest, final boolean expected) {
        //given

        //when
        final boolean actual = underTest.isRecoverable();

        //then
        Assertions.assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("isPurgeableProvider")
    void testIsPurgeableShouldReturnValidValueWhenCalled(final RecoveryLevel underTest, final boolean expected) {
        //given

        //when
        final boolean actual = underTest.isPurgeable();

        //then
        Assertions.assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("isSubscriptionProtectedProvider")
    void testIsSubscriptionProtectedShouldReturnValidValueWhenCalled(final RecoveryLevel underTest, final boolean expected) {
        //given

        //when
        final boolean actual = underTest.isSubscriptionProtected();

        //then
        Assertions.assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("isCustomizedProvider")
    void testIsCustomizedShouldReturnValidValueWhenCalled(final RecoveryLevel underTest, final boolean expected) {
        //given

        //when
        final boolean actual = underTest.isCustomized();

        //then
        Assertions.assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("checkValidityDaysProvider")
    void testCheckValidRecoverableDaysShouldReturnValidValueWhenCalled(
            final RecoveryLevel underTest, final Integer input, final boolean expectedToBeValid) {
        //given

        //when
        if (expectedToBeValid) {
            Assertions.assertDoesNotThrow(() -> underTest.checkValidRecoverableDays(input));
        } else {
            Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.checkValidRecoverableDays(input));
        }

        //then + no exception / exception
    }
}
