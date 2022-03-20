package com.github.nagyesta.lowkeyvault.openapi;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

class ExamplesTest {

    @Test
    void testConstructorShouldThrowExceptionWhenCalled() throws NoSuchMethodException {
        //given
        final Constructor<Examples> constructor = Examples.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        //when
        Assertions.assertThrows(InvocationTargetException.class, constructor::newInstance);

        //then + exception
    }
}
