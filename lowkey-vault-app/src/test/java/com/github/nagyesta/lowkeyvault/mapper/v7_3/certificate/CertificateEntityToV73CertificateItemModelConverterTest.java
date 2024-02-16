package com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate;

import com.github.nagyesta.lowkeyvault.TestConstantsCertificates;
import com.github.nagyesta.lowkeyvault.mapper.common.registry.CertificateConverterRegistry;
import com.github.nagyesta.lowkeyvault.model.common.ApiConstants;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.CertificatePropertiesModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.KeyVaultCertificateItemModel;
import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.github.nagyesta.lowkeyvault.TestConstants.TAGS_ONE_KEY;
import static com.github.nagyesta.lowkeyvault.TestConstantsCertificates.CERT_NAME_1;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOCALHOST_8443;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOWKEY_VAULT;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

class CertificateEntityToV73CertificateItemModelConverterTest {


    private static final byte[] THUMBPRINT = {0, 1, 2, 3, 4, 5, 6, 7, 8};

    @SuppressWarnings("DataFlowIssue")
    @Test
    void testConstructorShouldThrowExceptionWhenCalledWithNull() {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new CertificateEntityToV73CertificateItemModelConverter(null));

        //then + exception
    }

    @Test
    void testConvertCertificateIdShouldNotContainVersionWhenCalled() {
        //given
        final CertificateEntityToV73PropertiesModelConverter properties = mock(CertificateEntityToV73PropertiesModelConverter.class);
        final CertificateConverterRegistry registry = mock(CertificateConverterRegistry.class);
        when(registry.propertiesConverter(eq(ApiConstants.V_7_3))).thenReturn(properties);
        final CertificateEntityToV73CertificateItemModelConverter underTest =
                new CertificateEntityToV73CertificateItemModelConverter(registry);
        final ReadOnlyKeyVaultCertificateEntity input = mock(ReadOnlyKeyVaultCertificateEntity.class);
        when(input.getId()).thenReturn(TestConstantsCertificates.VERSIONED_CERT_ENTITY_ID_1_VERSION_3);

        //when
        final String actual = underTest.convertCertificateId(input, HTTPS_LOWKEY_VAULT);

        //then
        Assertions.assertEquals(HTTPS_LOWKEY_VAULT + "/certificates/" + CERT_NAME_1, actual);
    }

    @Test
    void testConvertShouldMapThumbprintWhenCalledWithValidData() {
        //given
        final CertificateEntityToV73PropertiesModelConverter properties = mock(CertificateEntityToV73PropertiesModelConverter.class);
        final CertificateConverterRegistry registry = mock(CertificateConverterRegistry.class);
        when(registry.propertiesConverter(eq(ApiConstants.V_7_5))).thenReturn(properties);
        final CertificateEntityToV73CertificateItemModelConverter underTest =
                new CertificateEntityToV73CertificateItemModelConverter(registry);
        final byte[] expectedThumbprint = THUMBPRINT;
        final ReadOnlyKeyVaultCertificateEntity input = mock(ReadOnlyKeyVaultCertificateEntity.class);
        final CertificatePropertiesModel propertiesModel = new CertificatePropertiesModel();
        when(properties.convert(same(input), eq(HTTPS_LOCALHOST_8443))).thenReturn(propertiesModel);
        when(input.getId()).thenReturn(TestConstantsCertificates.VERSIONED_CERT_ENTITY_ID_1_VERSION_3);
        when(input.getThumbprint()).thenReturn(expectedThumbprint);
        when(input.getTags()).thenReturn(TAGS_ONE_KEY);

        //when
        final KeyVaultCertificateItemModel actual = underTest.convert(input, HTTPS_LOCALHOST_8443);

        //then
        Assertions.assertEquals(HTTPS_LOCALHOST_8443 + "/certificates/" + CERT_NAME_1, actual.getCertificateId());
        Assertions.assertArrayEquals(expectedThumbprint, actual.getThumbprint());
        Assertions.assertNotNull(actual.getAttributes());
        Assertions.assertSame(propertiesModel, actual.getAttributes());
        Assertions.assertTrue(actual.getAttributes().isEnabled());
        Assertions.assertEquals(TAGS_ONE_KEY, actual.getTags());
        verify(properties).convert(same(input), eq(HTTPS_LOCALHOST_8443));
    }
}
