package com.github.nagyesta.lowkeyvault.service.certificate.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyCurveName;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.service.exception.CryptoException;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.security.cert.X509Certificate;
import java.time.OffsetDateTime;

import static com.github.nagyesta.lowkeyvault.TestConstantsKeys.KEY_NAME_1;
import static com.github.nagyesta.lowkeyvault.TestConstantsKeys.VERSIONED_KEY_ENTITY_ID_1_VERSION_1;
import static org.mockito.Mockito.*;

class CertificateGeneratorTest {

    @Test
    void testGenerateCertificateShouldWrapExceptionWhenExceptionIsCausedDuringExecution() {
        //given
        final var vault = mock(VaultFake.class);
        when(vault.keyVaultFake()).thenThrow(new IllegalStateException());
        final var input = CertificateCreationInput.builder()
                .name(KEY_NAME_1)
                .certAuthorityType(CertAuthorityType.SELF_SIGNED)
                .subject("CN=localhost")
                .validityStart(OffsetDateTime.now())
                .contentType(CertContentType.PEM)
                .keyType(KeyType.EC)
                .keyCurveName(KeyCurveName.P_256K)
                .build();
        final var underTest = new CertificateGenerator(vault, VERSIONED_KEY_ENTITY_ID_1_VERSION_1);

        //when
        Assertions.assertThrows(CryptoException.class, () -> underTest.generateCertificate(input));

        //then + exception
        verify(vault).keyVaultFake();
    }

    @Test
    void testGenerateCertificateSigningRequestShouldWrapExceptionWhenExceptionIsCausedDuringExecution() {
        //given
        final var vault = mock(VaultFake.class);
        when(vault.keyVaultFake()).thenThrow(new IllegalStateException());
        final var certificate = mock(X509Certificate.class);
        final var underTest = new CertificateGenerator(vault, VERSIONED_KEY_ENTITY_ID_1_VERSION_1);

        //when
        Assertions.assertThrows(CryptoException.class, () -> underTest.generateCertificateSigningRequest(KEY_NAME_1, certificate));

        //then + exception
        verify(vault).keyVaultFake();
    }
}
