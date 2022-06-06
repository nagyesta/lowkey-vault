package com.github.nagyesta.lowkeyvault.model.v7_3.key;

import com.github.nagyesta.lowkeyvault.service.key.constants.LifetimeActionTriggerType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Period;

class KeyLifetimeActionTriggerModelTest {

    @Test
    void testJsonConstructorShouldThrowExceptionWhenBothInputsAreNull() {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                new KeyLifetimeActionTriggerModel(null, null));

        //then + exception
    }

    @Test
    void testJsonConstructorShouldThrowExceptionWhenBothInputsArePresent() {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                new KeyLifetimeActionTriggerModel(Period.ZERO, Period.ZERO));

        //then + exception
    }

    @Test
    void testJsonConstructorShouldSetBeforeExpiryWhenItIsPopulated() {
        //given

        //when
        final KeyLifetimeActionTriggerModel actual = new KeyLifetimeActionTriggerModel(Period.ZERO, null);

        //then
        Assertions.assertEquals(Period.ZERO, actual.getTriggerPeriod());
        Assertions.assertEquals(LifetimeActionTriggerType.TIME_BEFORE_EXPIRY, actual.getTriggerType());
    }

    @Test
    void testJsonConstructorShouldSetAfterCreateWhenItIsPopulated() {
        //given

        //when
        final KeyLifetimeActionTriggerModel actual = new KeyLifetimeActionTriggerModel(null, Period.ZERO);

        //then
        Assertions.assertEquals(Period.ZERO, actual.getTriggerPeriod());
        Assertions.assertEquals(LifetimeActionTriggerType.TIME_AFTER_CREATE, actual.getTriggerType());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testConstructorShouldThrowExceptionWhenCalledWithNull() {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                new KeyLifetimeActionTriggerModel(null));

        //then + exception
    }
}
