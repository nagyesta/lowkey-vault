package com.github.nagyesta.lowkeyvault.model.v7_3.key;

import com.github.nagyesta.lowkeyvault.model.v7_3.key.constants.LifetimeActionType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class KeyLifetimeActionTypeModelTest {

    @SuppressWarnings("ConstantConditions")
    @Test
    void testConstructorShouldThrowExceptionWhenCalledWithNull() {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> new KeyLifetimeActionTypeModel(null));

        //then + exception
    }

    @Test
    void testConstructorShouldSetActionTypeWhenCalledWithValidValue() {
        //given
        final var expected = LifetimeActionType.NOTIFY;

        //when
        final var actual = new KeyLifetimeActionTypeModel(expected);

        //then
        Assertions.assertEquals(expected, actual.getType());
    }
}
