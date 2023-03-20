package com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CertificateEntityToV73PropertiesModelConverterTest {

    @SuppressWarnings("DataFlowIssue")
    @Test
    void testConstructorShouldThrowExceptionWhenCalledWithNull() {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> new CertificateEntityToV73PropertiesModelConverter(null));

        //then + exception
    }
}
