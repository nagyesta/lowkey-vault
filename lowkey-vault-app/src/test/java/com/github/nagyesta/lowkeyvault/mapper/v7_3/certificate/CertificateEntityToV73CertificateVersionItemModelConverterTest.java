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
import static org.mockito.ArgumentMatchers.eq;
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
        final CertificateConverterRegistry registry = mock(CertificateConverterRegistry.class);
        final CertificateEntityToV73PropertiesModelConverter properties = mock(CertificateEntityToV73PropertiesModelConverter.class);
        when(registry.propertiesConverter(eq(ApiConstants.V_7_3))).thenReturn(properties);
        registry.registerPropertiesConverter(properties);
        final CertificateEntityToV73CertificateVersionItemModelConverter underTest =
                new CertificateEntityToV73CertificateVersionItemModelConverter(registry);
        final ReadOnlyKeyVaultCertificateEntity input = mock(ReadOnlyKeyVaultCertificateEntity.class);
        when(input.getId()).thenReturn(TestConstantsCertificates.VERSIONED_CERT_ENTITY_ID_1_VERSION_3);

        //when
        final String actual = underTest.convertCertificateId(input, HTTPS_LOWKEY_VAULT);

        //then
        Assertions.assertEquals(HTTPS_LOWKEY_VAULT + "/certificates/" + CERT_NAME_1 + "/" + CERT_VERSION_3, actual);
    }
}
