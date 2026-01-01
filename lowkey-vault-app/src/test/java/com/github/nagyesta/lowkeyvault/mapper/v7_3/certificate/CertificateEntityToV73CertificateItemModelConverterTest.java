package com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate;

import com.github.nagyesta.lowkeyvault.TestConstantsCertificates;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.CertificatePropertiesModel;
import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static com.github.nagyesta.lowkeyvault.TestConstants.TAGS_ONE_KEY;
import static com.github.nagyesta.lowkeyvault.TestConstantsCertificates.CERT_NAME_1;
import static com.github.nagyesta.lowkeyvault.TestConstantsCertificates.CERT_VERSION_3;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOCALHOST_8443;
import static org.mockito.Mockito.*;

class CertificateEntityToV73CertificateItemModelConverterTest {


    private static final byte[] THUMBPRINT = {0, 1, 2, 3, 4, 5, 6, 7, 8};

    @Test
    void testConvertWithoutVersionShouldMapThumbprintWhenCalledWithValidData() {
        //given
        final var properties = mock(CertificateEntityToV73PropertiesModelConverter.class);
        final var underTest = new CertificateEntityToV73CertificateItemModelConverterImpl();
        ReflectionTestUtils.setField(underTest, "certificateEntityToV73PropertiesModelConverter", properties);
        final var expectedThumbprint = THUMBPRINT;
        final var input = mock(ReadOnlyKeyVaultCertificateEntity.class);
        final var propertiesModel = new CertificatePropertiesModel();
        when(properties.convert(same(input))).thenReturn(propertiesModel);
        when(input.getId()).thenReturn(TestConstantsCertificates.VERSIONED_CERT_ENTITY_ID_1_VERSION_3);
        when(input.getThumbprint()).thenReturn(expectedThumbprint);
        when(input.getTags()).thenReturn(TAGS_ONE_KEY);

        //when
        final var actual = underTest.convertWithoutVersion(input, HTTPS_LOCALHOST_8443);

        //then
        Assertions.assertEquals(HTTPS_LOCALHOST_8443 + "/certificates/" + CERT_NAME_1, actual.getCertificateId());
        Assertions.assertArrayEquals(expectedThumbprint, actual.getThumbprint());
        Assertions.assertNotNull(actual.getAttributes());
        Assertions.assertSame(propertiesModel, actual.getAttributes());
        Assertions.assertTrue(actual.getAttributes().isEnabled());
        Assertions.assertEquals(TAGS_ONE_KEY, actual.getTags());
        verify(properties).convert(same(input));
    }

    @Test
    void testConvertShouldMapThumbprintWhenCalledWithValidData() {
        //given
        final var properties = mock(CertificateEntityToV73PropertiesModelConverter.class);
        final var underTest = new CertificateEntityToV73CertificateItemModelConverterImpl();
        ReflectionTestUtils.setField(underTest, "certificateEntityToV73PropertiesModelConverter", properties);
        final var expectedThumbprint = THUMBPRINT;
        final var input = mock(ReadOnlyKeyVaultCertificateEntity.class);
        final var propertiesModel = new CertificatePropertiesModel();
        when(properties.convert(same(input))).thenReturn(propertiesModel);
        when(input.getId()).thenReturn(TestConstantsCertificates.VERSIONED_CERT_ENTITY_ID_1_VERSION_3);
        when(input.getThumbprint()).thenReturn(expectedThumbprint);
        when(input.getTags()).thenReturn(TAGS_ONE_KEY);

        //when
        final var actual = underTest.convert(input, HTTPS_LOCALHOST_8443);

        //then
        Assertions.assertEquals(HTTPS_LOCALHOST_8443 + "/certificates/" + CERT_NAME_1 + "/" + CERT_VERSION_3, actual.getCertificateId());
        Assertions.assertArrayEquals(expectedThumbprint, actual.getThumbprint());
        Assertions.assertNotNull(actual.getAttributes());
        Assertions.assertSame(propertiesModel, actual.getAttributes());
        Assertions.assertTrue(actual.getAttributes().isEnabled());
        Assertions.assertEquals(TAGS_ONE_KEY, actual.getTags());
        verify(properties).convert(same(input));
    }
}
