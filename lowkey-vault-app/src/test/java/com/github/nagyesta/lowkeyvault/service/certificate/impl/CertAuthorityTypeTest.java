package com.github.nagyesta.lowkeyvault.service.certificate.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class CertAuthorityTypeTest {

    public static Stream<Arguments> getValueProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(CertAuthorityType.SELF_SIGNED, "Self"))
                .add(Arguments.of(CertAuthorityType.UNKNOWN, "Unknown"))
                .build();
    }

    @ParameterizedTest
    @MethodSource("getValueProvider")
    void testGetValueShouldReturnPredefinedValueWhenCalled(final CertAuthorityType underTest, final String expected) {
        //given

        //when
        final String actual = underTest.getValue();

        //then
        Assertions.assertEquals(expected, actual);
    }
}
