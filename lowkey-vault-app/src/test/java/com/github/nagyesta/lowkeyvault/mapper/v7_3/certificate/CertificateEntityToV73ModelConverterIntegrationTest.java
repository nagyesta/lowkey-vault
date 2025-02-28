package com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate;

import com.github.nagyesta.lowkeyvault.mapper.common.registry.CertificateConverterRegistry;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyCurveName;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertContentType;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertificateCreationInput;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.KeyUsageEnum;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.KeyVaultCertificateEntity;
import com.github.nagyesta.lowkeyvault.service.exception.CryptoException;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.impl.VaultFakeImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.security.cert.CertificateEncodingException;
import java.util.Map;
import java.util.Set;

import static com.github.nagyesta.lowkeyvault.TestConstants.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsCertificates.CERT_NAME_1;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOCALHOST_8443;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOWKEY_VAULT;
import static com.github.nagyesta.lowkeyvault.service.certificate.impl.CertAuthorityType.SELF_SIGNED;
import static com.github.nagyesta.lowkeyvault.service.certificate.impl.CertAuthorityType.UNKNOWN;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

class CertificateEntityToV73ModelConverterIntegrationTest {

    private static final String NAME_AT_EXAMPLE_COM = "name@example.com";
    private static final int VALIDITY_MONTHS = 36;
    private static final int RSA_KEY_SIZE = 2048;

    @Test
    void testConvertShouldConvertValuableFieldsWhenCalledWithValidCertificate() throws CertificateEncodingException {
        //given
        final var registry = new CertificateConverterRegistry();
        new CertificateEntityToV73PropertiesModelConverter(registry).afterPropertiesSet();
        new CertificateEntityToV73PolicyModelConverter(registry).afterPropertiesSet();
        final var underTest = new CertificateEntityToV73ModelConverter(registry);
        final var input = CertificateCreationInput.builder()
                .validityStart(NOW)
                .subject("CN=" + LOCALHOST)
                .upns(Set.of(LOOP_BACK_IP))
                .name(CERT_NAME_1)
                .dnsNames(Set.of(LOWKEY_VAULT))
                .emails(Set.of(NAME_AT_EXAMPLE_COM))
                .enableTransparency(false)
                .certAuthorityType(UNKNOWN)
                .contentType(CertContentType.PEM)
                .certificateType(null)
                .keySize(RSA_KEY_SIZE)
                .keyType(KeyType.RSA)
                .extendedKeyUsage(Set.of("1.3.6.1.5.5.7.3.1", "1.3.6.1.5.5.7.3.2"))
                .keyUsage(Set.of(KeyUsageEnum.KEY_ENCIPHERMENT))
                .reuseKeyOnRenewal(true)
                .validityMonths(VALIDITY_MONTHS)
                .exportablePrivateKey(true)
                .build();
        final VaultFake vault = new VaultFakeImpl(HTTPS_LOWKEY_VAULT);
        final var source = new KeyVaultCertificateEntity(CERT_NAME_1, input, vault);
        final var id = source.getId();
        final var tags = Map.of(KEY_1, VALUE_1, KEY_2, VALUE_2, KEY_3, VALUE_3);
        source.setTags(tags);

        ///when
        final var actual = underTest.convert(source, HTTPS_LOCALHOST_8443);

        //then
        Assertions.assertEquals(id.asUri(HTTPS_LOCALHOST_8443).toString(), actual.getId());
        Assertions.assertEquals(source.getKid().asUri(HTTPS_LOCALHOST_8443).toString(), actual.getKid());
        Assertions.assertEquals(source.getSid().asUri(HTTPS_LOCALHOST_8443).toString(), actual.getSid());

        final var attributes = actual.getAttributes();
        Assertions.assertTrue(NOW.isBefore(attributes.getCreatedOn()),
                "CreatedOn should be later than the beginning of the test.");
        Assertions.assertTrue(NOW.isBefore(attributes.getUpdatedOn()),
                "UpdatedOn should be later than the beginning of the test.");
        Assertions.assertEquals(NOW, attributes.getNotBefore());
        Assertions.assertEquals(NOW.plusMonths(VALIDITY_MONTHS), attributes.getExpiresOn());
        Assertions.assertEquals(vault.getRecoveryLevel(), attributes.getRecoveryLevel());
        Assertions.assertEquals(vault.getRecoverableDays(), attributes.getRecoverableDays());

        final var policy = actual.getPolicy();
        Assertions.assertEquals(id.asPolicyUri(HTTPS_LOCALHOST_8443).toString(), policy.getId());
        final var issuer = policy.getIssuer();
        Assertions.assertEquals(UNKNOWN.getValue(), issuer.getIssuer());
        Assertions.assertNull(issuer.getCertType());

        Assertions.assertIterableEquals(tags.entrySet(), actual.getTags().entrySet());
        Assertions.assertArrayEquals(source.getCertificate().getEncoded(), actual.getCertificate());
        Assertions.assertArrayEquals(source.getThumbprint(), actual.getThumbprint());
    }

    @Test
    void testGetThumbprintShouldWrapExceptionWhenThumbprintCalculationFails() {
        //given
        final var input = CertificateCreationInput.builder()
                .validityStart(NOW)
                .subject("CN=" + LOCALHOST)
                .name(CERT_NAME_1)
                .enableTransparency(false)
                .certAuthorityType(SELF_SIGNED)
                .contentType(CertContentType.PKCS12)
                .keyCurveName(KeyCurveName.P_521)
                .keyType(KeyType.EC)
                .validityMonths(VALIDITY_MONTHS)
                .build();
        final VaultFake vault = new VaultFakeImpl(HTTPS_LOWKEY_VAULT);
        final var source = new KeyVaultCertificateEntity(CERT_NAME_1, input, vault);
        final var underTest = spy(source);

        doThrow(new IllegalArgumentException("fail")).when(underTest).getEncodedCertificate();

        ///when
        Assertions.assertThrows(CryptoException.class, underTest::getThumbprint);

        //then + exception
    }
}
