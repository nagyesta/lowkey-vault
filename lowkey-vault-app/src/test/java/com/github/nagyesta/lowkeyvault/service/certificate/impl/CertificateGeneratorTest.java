package com.github.nagyesta.lowkeyvault.service.certificate.impl;

import com.github.nagyesta.lowkeyvault.service.exception.CryptoException;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.security.cert.X509Certificate;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstantsKeys.KEY_NAME_1;
import static com.github.nagyesta.lowkeyvault.TestConstantsKeys.VERSIONED_KEY_ENTITY_ID_1_VERSION_1;
import static org.mockito.Mockito.*;

class CertificateGeneratorTest {

    public static Stream<Arguments> nullProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null))
                .add(Arguments.of(mock(VaultFake.class), null))
                .add(Arguments.of(null, VERSIONED_KEY_ENTITY_ID_1_VERSION_1))
                .build();
    }

    @ParameterizedTest
    @MethodSource("nullProvider")
    void testConstructorShouldThrowExceptionWhenCalledWithNulls(final VaultFake vault, final VersionedKeyEntityId kid) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> new CertificateGenerator(vault, kid));

        //then + exception
    }

    @Test
    void testGenerateCertificateShouldWrapExceptionWhenExceptionIsCausedDuringExecution() {
        //given
        final var vault = mock(VaultFake.class);
        when(vault.keyVaultFake()).thenThrow(new IllegalStateException());
        final var input = CertificateCreationInput.builder().name(KEY_NAME_1).build();
        final var underTest = new CertificateGenerator(vault, VERSIONED_KEY_ENTITY_ID_1_VERSION_1);

        //when
        Assertions.assertThrows(CryptoException.class, () -> underTest.generateCertificate(input));

        //then + exception
        verify(vault).keyVaultFake();
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testGenerateCertificateShouldThrowExceptionWhenCalledWithNull() {
        //given
        final var vault = mock(VaultFake.class);
        final var underTest = new CertificateGenerator(vault, VERSIONED_KEY_ENTITY_ID_1_VERSION_1);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.generateCertificate(null));

        //then + exception
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

    @SuppressWarnings("ConstantConditions")
    @Test
    void testGenerateCertificateSigningRequestShouldThrowExceptionWhenCalledWithNullName() {
        //given
        final var vault = mock(VaultFake.class);
        final var underTest = new CertificateGenerator(vault, VERSIONED_KEY_ENTITY_ID_1_VERSION_1);
        final var certificate = mock(X509Certificate.class);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.generateCertificateSigningRequest(null, certificate));

        //then + exception
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testGenerateCertificateSigningRequestShouldThrowExceptionWhenCalledWithNullCertificate() {
        //given
        final var vault = mock(VaultFake.class);
        final var underTest = new CertificateGenerator(vault, VERSIONED_KEY_ENTITY_ID_1_VERSION_1);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.generateCertificateSigningRequest(KEY_NAME_1, null));

        //then + exception
    }
}
