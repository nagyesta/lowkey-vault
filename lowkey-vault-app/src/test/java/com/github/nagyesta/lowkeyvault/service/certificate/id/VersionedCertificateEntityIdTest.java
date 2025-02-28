package com.github.nagyesta.lowkeyvault.service.certificate.id;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstantsCertificates.CERT_NAME_1;
import static com.github.nagyesta.lowkeyvault.TestConstantsCertificates.CERT_VERSION_1;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOCALHOST_8443;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOWKEY_VAULT;

class VersionedCertificateEntityIdTest {

    public static Stream<Arguments> versionedNullProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, CERT_NAME_1, CERT_VERSION_1))
                .add(Arguments.of(HTTPS_LOCALHOST_8443, null, CERT_VERSION_1))
                .add(Arguments.of(HTTPS_LOCALHOST_8443, CERT_NAME_1, null))
                .build();
    }

    public static Stream<Arguments> versionlessNullProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null))
                .add(Arguments.of(HTTPS_LOCALHOST_8443, null))
                .add(Arguments.of(null, CERT_NAME_1))
                .build();
    }

    @ParameterizedTest
    @MethodSource("versionedNullProvider")
    void testConstructorShouldThrowExceptionWhenCalledWithNull(final URI vault, final String name, final String version) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> new VersionedCertificateEntityId(vault, name, version));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("versionlessNullProvider")
    void testConstructorWithoutVersionShouldThrowExceptionWhenCalledWithNull(final URI vault, final String name) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> new VersionedCertificateEntityId(vault, name));

        //then + exception
    }

    @Test
    void testConstructorWithoutVersionShouldGenerateVersionWhenCalledWithoutValidInput() {
        //given

        //when
        final var actual = new VersionedCertificateEntityId(HTTPS_LOWKEY_VAULT, CERT_NAME_1);

        //then
        Assertions.assertNotNull(actual.version());
    }
}
