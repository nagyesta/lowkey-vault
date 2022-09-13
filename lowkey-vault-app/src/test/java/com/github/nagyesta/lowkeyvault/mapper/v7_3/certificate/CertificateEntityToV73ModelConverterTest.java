package com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.mockito.Mockito.mock;

class CertificateEntityToV73ModelConverterTest {

    public static Stream<Arguments> nullProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null))
                .add(Arguments.of(mock(CertificateEntityToV73PropertiesModelConverter.class), null))
                .add(Arguments.of(null, mock(CertificateEntityToV73PolicyModelConverter.class)))
                .build();
    }

    @ParameterizedTest
    @MethodSource("nullProvider")
    void testConstructorShouldThrowExceptionWhenCalledWithNull(
            final CertificateEntityToV73PropertiesModelConverter propertiesConverter,
            final CertificateEntityToV73PolicyModelConverter policyConverter) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new CertificateEntityToV73ModelConverter(propertiesConverter, policyConverter));

        //then + exceptions
    }
}
