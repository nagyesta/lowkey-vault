package com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate;

import com.github.nagyesta.lowkeyvault.TestConstantsCertificates;
import com.github.nagyesta.lowkeyvault.mapper.common.registry.CertificateConverterRegistry;
import com.github.nagyesta.lowkeyvault.model.common.ApiConstants;
import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.github.nagyesta.lowkeyvault.TestConstantsCertificates.CERT_NAME_1;
import static com.github.nagyesta.lowkeyvault.TestConstantsCertificates.CERT_VERSION_3;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOWKEY_VAULT;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CertificateEntityToV73CertificateVersionItemModelConverterTest {


    @SuppressWarnings("DataFlowIssue")
    @Test
    void testConstructorShouldThrowExceptionWhenCalledWithNull() {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new CertificateEntityToV73CertificateVersionItemModelConverter(null));

        //then + exception
    }

    @Test
    void testConvertCertificateIdShouldContainVersionWhenCalled() {
        //given
        final var registry = mock(CertificateConverterRegistry.class);
        final var properties = mock(CertificateEntityToV73PropertiesModelConverter.class);
        when(registry.propertiesConverter(ApiConstants.V_7_3)).thenReturn(properties);
        registry.registerPropertiesConverter(properties);
        final var underTest =
                new CertificateEntityToV73CertificateVersionItemModelConverter(registry);
        final var input = mock(ReadOnlyKeyVaultCertificateEntity.class);
        when(input.getId()).thenReturn(TestConstantsCertificates.VERSIONED_CERT_ENTITY_ID_1_VERSION_3);

        //when
        final var actual = underTest.convertCertificateId(input, HTTPS_LOWKEY_VAULT);

        //then
        Assertions.assertEquals(HTTPS_LOWKEY_VAULT + "/certificates/" + CERT_NAME_1 + "/" + CERT_VERSION_3, actual);
    }
}
