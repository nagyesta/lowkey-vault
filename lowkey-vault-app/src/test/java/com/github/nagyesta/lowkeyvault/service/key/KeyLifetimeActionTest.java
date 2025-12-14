package com.github.nagyesta.lowkeyvault.service.key;

import com.github.nagyesta.lowkeyvault.model.v7_3.key.constants.LifetimeActionType;
import com.github.nagyesta.lowkeyvault.service.key.constants.LifetimeActionTriggerType;
import com.github.nagyesta.lowkeyvault.service.key.impl.KeyLifetimeActionTrigger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Period;

class KeyLifetimeActionTest {

    @Test
    void testConstructorShouldSetValuesWhenCalledWithValidInput() {
        //given
        final var type = LifetimeActionType.NOTIFY;
        final LifetimeActionTrigger trigger = new KeyLifetimeActionTrigger(Period.ZERO, LifetimeActionTriggerType.TIME_BEFORE_EXPIRY);

        //when
        final var actual = new KeyLifetimeAction(type, trigger);

        //then
        Assertions.assertEquals(trigger, actual.trigger());
        Assertions.assertEquals(type, actual.actionType());
    }
}
