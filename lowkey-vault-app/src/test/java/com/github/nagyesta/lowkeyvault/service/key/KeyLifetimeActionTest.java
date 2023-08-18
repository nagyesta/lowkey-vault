package com.github.nagyesta.lowkeyvault.service.key;

import com.github.nagyesta.lowkeyvault.model.v7_3.key.constants.LifetimeActionType;
import com.github.nagyesta.lowkeyvault.service.key.constants.LifetimeActionTriggerType;
import com.github.nagyesta.lowkeyvault.service.key.impl.KeyLifetimeActionTrigger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Period;
import java.util.stream.Stream;

class KeyLifetimeActionTest {

    public static Stream<Arguments> invalidProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null))
                .add(Arguments.of(null, new KeyLifetimeActionTrigger(Period.ZERO, LifetimeActionTriggerType.TIME_BEFORE_EXPIRY)))
                .add(Arguments.of(LifetimeActionType.NOTIFY, null))
                .build();
    }

    @ParameterizedTest
    @MethodSource("invalidProvider")
    void testConstructorShouldThrowExceptionWhenCalledWithNulls(final LifetimeActionType type, final LifetimeActionTrigger trigger) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> new KeyLifetimeAction(type, trigger));

        //then + exception
    }

    @Test
    void testConstructorShouldSetValuesWhenCalledWithValidInput() {
        //given
        final LifetimeActionType type = LifetimeActionType.NOTIFY;
        final LifetimeActionTrigger trigger = new KeyLifetimeActionTrigger(Period.ZERO, LifetimeActionTriggerType.TIME_BEFORE_EXPIRY);

        //when
        final KeyLifetimeAction actual = new KeyLifetimeAction(type, trigger);

        //then
        Assertions.assertEquals(trigger, actual.trigger());
        Assertions.assertEquals(type, actual.actionType());
    }
}
