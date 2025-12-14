package com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyCurveName;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertContentType;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertificateCreationInput;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.KeyVaultCertificateEntity;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.impl.VaultFakeImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.github.nagyesta.lowkeyvault.TestConstants.LOCALHOST;
import static com.github.nagyesta.lowkeyvault.TestConstants.NOW;
import static com.github.nagyesta.lowkeyvault.TestConstantsCertificates.CERT_NAME_1;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOWKEY_VAULT;
import static com.github.nagyesta.lowkeyvault.service.certificate.impl.CertAuthorityType.SELF_SIGNED;

class CertificateEntityToV73PendingCertificateOperationModelConverterIntegrationTest {

    private static final int VALIDITY_MONTHS = 12;

    @Test
    void testConvertShouldConvertValuableFieldsWhenCalledWithValidInput() {
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
        final var underTest = new CertificateEntityToV73PendingCertificateOperationModelConverter();

        //when
        final var actual = underTest.convert(source, vault.baseUri());

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(source.getId().asPendingOperationUri(vault.baseUri()).toString(), actual.getId());
        Assertions.assertEquals(source.getId().asUriNoVersion(vault.baseUri()).toString(), actual.getTarget());
        Assertions.assertNotNull(actual.getCsr());
        Assertions.assertEquals("Self", actual.getIssuer().getIssuer());
        Assertions.assertEquals("completed", actual.getStatus());
        Assertions.assertNull(actual.getStatusDetails());
        Assertions.assertNotNull(actual.getRequestId());
        Assertions.assertFalse(actual.isCancellationRequested());
    }
}
