package com.github.nagyesta.lowkeyvault.template.backup;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class BackupContextTest {

    public static Stream<Arguments> validProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of("a", 1))
                .add(Arguments.of("b", 2))
                .add(Arguments.of("c", 0))
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testConstructorShouldThrowExceptionWhenCalledWithNullHost() {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> new BackupContext(null, 0));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("validProvider")
    void testConstructorShouldSetValuesWhenCalledWithValidData(final String host, final int port) {
        //given

        //when
        final BackupContext actual = new BackupContext(host, port);

        //then
        Assertions.assertEquals(host, actual.getHost());
        Assertions.assertEquals(port, actual.getPort());
    }
}
