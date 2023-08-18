package com.github.nagyesta.lowkeyvault.service.key.impl;

import com.github.nagyesta.lowkeyvault.model.v7_3.key.constants.LifetimeActionType;
import com.github.nagyesta.lowkeyvault.service.key.KeyLifetimeAction;
import com.github.nagyesta.lowkeyvault.service.key.LifetimeAction;
import com.github.nagyesta.lowkeyvault.service.key.constants.LifetimeActionTriggerType;
import com.github.nagyesta.lowkeyvault.service.key.id.KeyEntityId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.OffsetDateTime;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstants.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsKeys.UNVERSIONED_KEY_ENTITY_ID_1;

class KeyRotationPolicyTest {

    private static final String DAYS_42 = "P42D";
    private static final String DAYS_100 = "P100D";
    private static final String DAYS_7 = "P7D";
    private static final KeyLifetimeActionTrigger TRIGGER_7_DAYS_BEFORE_EXPIRY =
            new KeyLifetimeActionTrigger(Period.parse(DAYS_7), LifetimeActionTriggerType.TIME_BEFORE_EXPIRY);
    private static final KeyLifetimeActionTrigger TRIGGER_42_DAYS_BEFORE_EXPIRY =
            new KeyLifetimeActionTrigger(Period.parse(DAYS_42), LifetimeActionTriggerType.TIME_BEFORE_EXPIRY);
    private static final KeyLifetimeAction NOTIFY_7_DAYS_BEFORE_EXPIRY =
            new KeyLifetimeAction(LifetimeActionType.NOTIFY, TRIGGER_7_DAYS_BEFORE_EXPIRY);
    private static final KeyLifetimeAction NOTIFY_42_DAYS_BEFORE_EXPIRY =
            new KeyLifetimeAction(LifetimeActionType.NOTIFY, TRIGGER_42_DAYS_BEFORE_EXPIRY);

    private static final KeyLifetimeActionTrigger TRIGGER_42_DAYS_AFTER_CREATE =
            new KeyLifetimeActionTrigger(Period.parse(DAYS_42), LifetimeActionTriggerType.TIME_AFTER_CREATE);
    private static final KeyLifetimeAction ROTATE_42_DAYS_AFTER_CREATE =
            new KeyLifetimeAction(LifetimeActionType.ROTATE, TRIGGER_42_DAYS_AFTER_CREATE);
    private static final int OFFSET_SECONDS_10_MINUTES = 600;

    @SuppressWarnings("checkstyle:MagicNumber")
    public static Stream<Arguments> invalidDataProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(OffsetDateTime.now().plusDays(10), Period.ofDays(120),
                        Period.ofDays(30), LifetimeActionTriggerType.TIME_BEFORE_EXPIRY,
                        Period.ofDays(115), LifetimeActionTriggerType.TIME_AFTER_CREATE, false))
                .add(Arguments.of(OffsetDateTime.now().plusDays(10), Period.ofDays(20),
                        Period.ofDays(7), LifetimeActionTriggerType.TIME_BEFORE_EXPIRY,
                        Period.ofDays(13), LifetimeActionTriggerType.TIME_AFTER_CREATE, false))
                .add(Arguments.of(OffsetDateTime.now().plusDays(10), Period.ofDays(120),
                        Period.ofDays(7), LifetimeActionTriggerType.TIME_BEFORE_EXPIRY,
                        Period.ofDays(13), LifetimeActionTriggerType.TIME_BEFORE_EXPIRY, true))
                .add(Arguments.of(OffsetDateTime.now().plusDays(10), Period.ofDays(120),
                        Period.ofDays(30), LifetimeActionTriggerType.TIME_AFTER_CREATE,
                        Period.ofDays(100), LifetimeActionTriggerType.TIME_AFTER_CREATE, false))
                .build();
    }

    public static Stream<Arguments> invalidProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, Period.ZERO, Map.of()))
                .add(Arguments.of(UNVERSIONED_KEY_ENTITY_ID_1, null, Map.of()))
                .add(Arguments.of(UNVERSIONED_KEY_ENTITY_ID_1, Period.ZERO, null))
                .build();
    }

    public static Stream<Arguments> autoRotateActionProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(Map.of(
                        LifetimeActionType.NOTIFY, NOTIFY_7_DAYS_BEFORE_EXPIRY,
                        LifetimeActionType.ROTATE, ROTATE_42_DAYS_AFTER_CREATE), true))
                .add(Arguments.of(Map.of(
                        LifetimeActionType.ROTATE, ROTATE_42_DAYS_AFTER_CREATE), true))
                .add(Arguments.of(Map.of(
                        LifetimeActionType.NOTIFY, NOTIFY_7_DAYS_BEFORE_EXPIRY), false))
                .add(Arguments.of(Map.of(), false))
                .build();
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    public static Stream<Arguments> missedRotationsProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments
                        .of(10, //keyCreatedDaysAgo
                                10, //policyCreatedDaysAgo
                                30, //policyExpiryDays
                                20, //policyRotatePeriod
                                LifetimeActionTriggerType.TIME_AFTER_CREATE,
                                List.of() //expectedRotationsDaysAgo
                        ))
                .add(Arguments
                        .of(40, //keyCreatedDaysAgo
                                10, //policyCreatedDaysAgo
                                35, //policyExpiryDays
                                25, //policyRotatePeriod
                                LifetimeActionTriggerType.TIME_AFTER_CREATE,
                                List.of(10) //expectedRotationsDaysAgo
                        ))
                .add(Arguments
                        .of(120, //keyCreatedDaysAgo
                                110, //policyCreatedDaysAgo
                                90, //policyExpiryDays
                                20, //policyRotatePeriod
                                LifetimeActionTriggerType.TIME_AFTER_CREATE,
                                List.of(100, 80, 60, 40, 20, 0) //expectedRotationsDaysAgo
                        ))
                .add(Arguments
                        .of(120, //keyCreatedDaysAgo
                                110, //policyCreatedDaysAgo
                                90, //policyExpiryDays
                                20, //policyRotatePeriod
                                LifetimeActionTriggerType.TIME_BEFORE_EXPIRY,
                                List.of(50) //expectedRotationsDaysAgo
                        ))
                .add(Arguments
                        .of(30, //keyCreatedDaysAgo
                                30, //policyCreatedDaysAgo
                                30, //policyExpiryDays
                                7, //policyRotatePeriod
                                LifetimeActionTriggerType.TIME_BEFORE_EXPIRY,
                                List.of(7) //expectedRotationsDaysAgo
                        ))
                .build();
    }

    @ParameterizedTest
    @MethodSource("invalidProvider")
    void testConstructorShouldThrowExceptionWhenCalledWithNull(final KeyEntityId keyEntityId,
                                                               final Period period,
                                                               final Map<LifetimeActionType, LifetimeAction> actions) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> new KeyRotationPolicy(keyEntityId, period, actions));

        //then + exception
    }

    @Test
    void testSetCreatedOnShouldUpdateValueWhenCalledWithValidInput() {
        //given
        final KeyRotationPolicy underTest = new KeyRotationPolicy(UNVERSIONED_KEY_ENTITY_ID_1, Period.ZERO, Map.of());
        final OffsetDateTime original = underTest.getCreatedOn();

        //when
        underTest.setCreatedOn(TIME_10_MINUTES_AGO);
        final OffsetDateTime actual = underTest.getCreatedOn();

        //then
        Assertions.assertNotEquals(original, actual);
        Assertions.assertEquals(TIME_10_MINUTES_AGO, actual);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testSetCreatedOnShouldThrowExceptionWhenCalledWithNull() {
        //given
        final KeyRotationPolicy underTest = new KeyRotationPolicy(UNVERSIONED_KEY_ENTITY_ID_1, Period.ZERO, Map.of());

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.setCreatedOn(null));

        //then + exception
    }

    @Test
    void testSetUpdatedOnShouldUpdateValueWhenCalledWithValidInput() {
        //given
        final KeyRotationPolicy underTest = new KeyRotationPolicy(UNVERSIONED_KEY_ENTITY_ID_1, Period.ZERO, Map.of());
        final OffsetDateTime original = underTest.getUpdatedOn();

        //when
        underTest.setUpdatedOn(TIME_10_MINUTES_AGO);
        final OffsetDateTime actual = underTest.getUpdatedOn();

        //then
        Assertions.assertNotEquals(original, actual);
        Assertions.assertEquals(TIME_10_MINUTES_AGO, actual);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testSetUpdatedOnShouldThrowExceptionWhenCalledWithNull() {
        //given
        final KeyRotationPolicy underTest = new KeyRotationPolicy(UNVERSIONED_KEY_ENTITY_ID_1, Period.ZERO, Map.of());

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.setUpdatedOn(null));

        //then + exception
    }

    @Test
    void testSetExpiryTimeShouldUpdateValueWhenCalledWithValidInput() {
        //given
        final KeyRotationPolicy underTest = new KeyRotationPolicy(UNVERSIONED_KEY_ENTITY_ID_1, Period.ZERO, Map.of());
        final Period original = underTest.getExpiryTime();

        //when
        underTest.setExpiryTime(Period.parse(DAYS_42));
        final Period actual = underTest.getExpiryTime();

        //then
        Assertions.assertEquals(Period.ZERO, original);
        Assertions.assertEquals(Period.parse(DAYS_42), actual);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testSetExpiryTimeShouldThrowExceptionWhenCalledWithNull() {
        //given
        final KeyRotationPolicy underTest = new KeyRotationPolicy(UNVERSIONED_KEY_ENTITY_ID_1, Period.ZERO, Map.of());

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.setExpiryTime(null));

        //then + exception
    }

    @Test
    void testSetExpiryTimeShouldUpdateUpdatedOnWhenCalledWithValidInput() {
        //given
        final KeyRotationPolicy underTest = new KeyRotationPolicy(UNVERSIONED_KEY_ENTITY_ID_1, Period.ZERO, Map.of());
        underTest.setUpdatedOn(TIME_10_MINUTES_AGO);

        //when
        underTest.setExpiryTime(Period.parse(DAYS_42));
        final OffsetDateTime actual = underTest.getUpdatedOn();

        //then
        Assertions.assertTrue(actual.isAfter(TIME_10_MINUTES_AGO));
    }

    @Test
    void testSetLifetimeActionsShouldUpdateValueWhenCalledWithValidInput() {
        //given
        final KeyRotationPolicy underTest = new KeyRotationPolicy(UNVERSIONED_KEY_ENTITY_ID_1, Period.ZERO, Map.of());
        final Map<LifetimeActionType, LifetimeAction> original = underTest.getLifetimeActions();
        final Map<LifetimeActionType, LifetimeAction> map = Map.of(LifetimeActionType.NOTIFY, NOTIFY_42_DAYS_BEFORE_EXPIRY);

        //when
        underTest.setLifetimeActions(map);
        final Map<LifetimeActionType, LifetimeAction> actual = underTest.getLifetimeActions();

        //then
        Assertions.assertTrue(original.isEmpty());
        Assertions.assertIterableEquals(map.entrySet(), actual.entrySet());
    }

    @Test
    void testSetLifetimeActionsShouldUpdateValueWhenNotifyIsReplacedWithAnotherNotify() {
        //given
        final Map<LifetimeActionType, LifetimeAction> map = Map.of(LifetimeActionType.NOTIFY, NOTIFY_42_DAYS_BEFORE_EXPIRY);
        final KeyRotationPolicy underTest = new KeyRotationPolicy(UNVERSIONED_KEY_ENTITY_ID_1, Period.parse(DAYS_100), map);
        final Map<LifetimeActionType, LifetimeAction> original = underTest.getLifetimeActions();
        final Map<LifetimeActionType, LifetimeAction> newValue = Map.of(
                LifetimeActionType.NOTIFY, NOTIFY_7_DAYS_BEFORE_EXPIRY,
                LifetimeActionType.ROTATE, ROTATE_42_DAYS_AFTER_CREATE);

        //when
        underTest.setLifetimeActions(newValue);
        final Map<LifetimeActionType, LifetimeAction> actual = underTest.getLifetimeActions();

        //then
        Assertions.assertIterableEquals(map.entrySet(), original.entrySet());
        Assertions.assertIterableEquals(newValue.entrySet(), actual.entrySet());
    }

    @Test
    void testSetLifetimeActionsShouldThrowExceptionWhenNotifyIsBeingRemoved() {
        //given
        final Map<LifetimeActionType, LifetimeAction> map = Map.of(LifetimeActionType.NOTIFY, NOTIFY_42_DAYS_BEFORE_EXPIRY);
        final KeyRotationPolicy underTest = new KeyRotationPolicy(UNVERSIONED_KEY_ENTITY_ID_1, Period.ZERO, map);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.setLifetimeActions(Map.of()));

        //then + exception
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testSetLifetimeActionsShouldThrowExceptionWhenCalledWithNull() {
        //given
        final KeyRotationPolicy underTest = new KeyRotationPolicy(UNVERSIONED_KEY_ENTITY_ID_1, Period.ZERO, Map.of());

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.setLifetimeActions(null));

        //then + exception
    }

    @Test
    void testSetLifetimeActionsShouldUpdateUpdatedOnWhenCalledWithValidInput() {
        //given
        final KeyRotationPolicy underTest = new KeyRotationPolicy(UNVERSIONED_KEY_ENTITY_ID_1, Period.ZERO, Map.of());
        underTest.setUpdatedOn(TIME_10_MINUTES_AGO);

        //when
        underTest.setLifetimeActions(Map.of());
        final OffsetDateTime actual = underTest.getUpdatedOn();

        //then
        Assertions.assertTrue(actual.isAfter(TIME_10_MINUTES_AGO));
    }

    @Test
    void testTimeShiftShouldAdjustCreatedOnAndUpdatedOnWhenCalledWithValidData() {
        //given
        final KeyRotationPolicy underTest = new KeyRotationPolicy(UNVERSIONED_KEY_ENTITY_ID_1, Period.ZERO, Map.of());
        underTest.setCreatedOn(NOW);
        underTest.setUpdatedOn(TIME_IN_10_MINUTES);

        //when
        underTest.timeShift(OFFSET_SECONDS_10_MINUTES);

        //then
        Assertions.assertEquals(TIME_10_MINUTES_AGO, underTest.getCreatedOn());
        Assertions.assertEquals(NOW, underTest.getUpdatedOn());
    }

    @ParameterizedTest
    @ValueSource(ints = {-42, -2, -1, 0})
    void testTimeShiftShouldThrowExceptionWhenCalledWithInvalidValue(final int value) {
        //given
        final KeyRotationPolicy underTest = new KeyRotationPolicy(UNVERSIONED_KEY_ENTITY_ID_1, Period.ZERO, Map.of());

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.timeShift(value));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("invalidDataProvider")
    void testValidateShouldThrowExceptionWhenDataIsInvalid(
            final OffsetDateTime expiryTime, final Period expiryPeriod,
            final Period notifyPeriod, final LifetimeActionTriggerType notifyTriggerType,
            final Period rotatePeriod, final LifetimeActionTriggerType rotateTriggerType,
            final boolean valid) {
        //given
        final KeyLifetimeActionTrigger notifyTrigger = new KeyLifetimeActionTrigger(notifyPeriod, notifyTriggerType);
        final KeyLifetimeAction notify = new KeyLifetimeAction(LifetimeActionType.NOTIFY, notifyTrigger);
        final KeyLifetimeActionTrigger rotateTrigger = new KeyLifetimeActionTrigger(rotatePeriod, rotateTriggerType);
        final KeyLifetimeAction rotate = new KeyLifetimeAction(LifetimeActionType.ROTATE, rotateTrigger);
        final KeyRotationPolicy underTest = new KeyRotationPolicy(UNVERSIONED_KEY_ENTITY_ID_1, expiryPeriod,
                Map.of(notify.actionType(), notify, rotate.actionType(), rotate));

        //when
        if (valid) {
            Assertions.assertDoesNotThrow(() -> underTest.validate(expiryTime));
        } else {
            Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.validate(expiryTime));
        }

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("autoRotateActionProvider")
    void testIsAutoRotateShouldReturnTrueWhenRotateActionIsPresent(
            final Map<LifetimeActionType, LifetimeAction> input, final boolean expected) {
        //given
        final KeyRotationPolicy underTest = new KeyRotationPolicy(UNVERSIONED_KEY_ENTITY_ID_1, Period.parse(DAYS_100), input);

        //when
        final boolean actual = underTest.isAutoRotate();

        //then
        Assertions.assertEquals(expected, actual);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testMissedRotationsShouldThrowExceptionWhenCalledWithNull() {
        //given
        final KeyRotationPolicy underTest = new KeyRotationPolicy(UNVERSIONED_KEY_ENTITY_ID_1, Period.parse(DAYS_100),
                Map.of(LifetimeActionType.ROTATE, ROTATE_42_DAYS_AFTER_CREATE));

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.missedRotations(null));

        //then + exception
    }

    @Test
    void testMissedRotationsShouldThrowExceptionWhenThereIsNoRotateAction() {
        //given
        final KeyRotationPolicy underTest = new KeyRotationPolicy(UNVERSIONED_KEY_ENTITY_ID_1, Period.parse(DAYS_100), Map.of());

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.missedRotations(OffsetDateTime.now()));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("missedRotationsProvider")
    void testMissedRotationsShouldReturnExpectedRotationTimestampsWhenCalledWithValidInput(
            final int keyCreatedDaysAgo, final int policyCreatedDaysAgo, final int policyExpiryDays,
            final int policyRotatePeriod, final LifetimeActionTriggerType triggerType,
            final List<Integer> expectedRotationsDaysAgo) {
        //given
        final KeyLifetimeActionTrigger trigger = new KeyLifetimeActionTrigger(Period.ofDays(policyRotatePeriod), triggerType);
        final KeyRotationPolicy underTest = new KeyRotationPolicy(UNVERSIONED_KEY_ENTITY_ID_1, Period.ofDays(policyExpiryDays),
                Map.of(LifetimeActionType.ROTATE, new KeyLifetimeAction(LifetimeActionType.ROTATE, trigger)));
        underTest.setCreatedOn(NOW.minusDays(policyCreatedDaysAgo));
        underTest.setUpdatedOn(NOW.minusDays(policyCreatedDaysAgo));

        final OffsetDateTime keyCreatedOn = NOW.minusDays(keyCreatedDaysAgo);

        //when
        final List<OffsetDateTime> actual = underTest.missedRotations(keyCreatedOn);

        //then
        final List<OffsetDateTime> expected = expectedRotationsDaysAgo.stream()
                .map(NOW::minusDays)
                .collect(Collectors.toList());
        Assertions.assertIterableEquals(expected, actual);
    }
}
