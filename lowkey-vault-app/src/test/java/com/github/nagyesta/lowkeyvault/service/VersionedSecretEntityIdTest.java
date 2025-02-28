package com.github.nagyesta.lowkeyvault.service;

import com.github.nagyesta.lowkeyvault.service.secret.id.VersionedSecretEntityId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstantsSecrets.SECRET_NAME_1;
import static com.github.nagyesta.lowkeyvault.TestConstantsSecrets.SECRET_VERSION_1;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOCALHOST;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOCALHOST_8443;

class VersionedSecretEntityIdTest {

    public static Stream<Arguments> invalidMinimalParameterProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null))
                .add(Arguments.of(HTTPS_LOCALHOST, null))
                .add(Arguments.of(null, SECRET_NAME_1))
                .build();
    }

    public static Stream<Arguments> invalidParameterProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, SECRET_NAME_1, SECRET_VERSION_1))
                .add(Arguments.of(HTTPS_LOCALHOST, null, SECRET_VERSION_1))
                .add(Arguments.of(HTTPS_LOCALHOST, SECRET_NAME_1, null))
                .build();
    }

    @Test
    void testConstructorShouldGenerateAVersionWhenNotProvidedAsParameter() {
        //given
        final var underTest = new VersionedSecretEntityId(HTTPS_LOCALHOST, SECRET_NAME_1);

        //when
        final var actual = underTest.version();

        //then
        Assertions.assertNotNull(actual);
    }

    @Test
    void testConstructorShouldUseVersionWhenProvidedAsParameter() {
        //given
        final var underTest = new VersionedSecretEntityId(HTTPS_LOCALHOST, SECRET_NAME_1, SECRET_VERSION_1);

        //when
        final var actual = underTest.version();

        //then
        Assertions.assertEquals(SECRET_VERSION_1, actual);
    }

    @ParameterizedTest
    @MethodSource("invalidParameterProvider")
    void testConstructorShouldThrowExceptionWhenCalledWithNulls(
            final URI vault, final String id, final String version) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> new VersionedSecretEntityId(vault, id, version));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("invalidMinimalParameterProvider")
    void testConstructorShouldThrowExceptionWhenCalledWithNulls(
            final URI vault, final String id) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> new VersionedSecretEntityId(vault, id));

        //then + exception
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testAsUriNoVersionShouldThrowExceptionWhenCalledWithNull() {
        //given
        final var underTest = new VersionedSecretEntityId(HTTPS_LOCALHOST, SECRET_NAME_1, SECRET_VERSION_1);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.asUriNoVersion(null));

        //then + exception
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testAsUriShouldThrowExceptionWhenCalledWithNull() {
        //given
        final var underTest = new VersionedSecretEntityId(HTTPS_LOCALHOST, SECRET_NAME_1, SECRET_VERSION_1);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.asUri(null));

        //then + exception
    }

    @Test
    void testAsUriShouldGenerateUriWhenCalledBasedOnProvidedValues() {
        //given
        final var underTest = new VersionedSecretEntityId(HTTPS_LOCALHOST, SECRET_NAME_1, SECRET_VERSION_1);

        //when
        final var actual = underTest.asUri(HTTPS_LOCALHOST_8443);

        //then
        Assertions.assertEquals(URI.create("https://localhost:8443/secrets/" + SECRET_NAME_1 + "/" + SECRET_VERSION_1), actual);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testAsRecoveryUriShouldThrowExceptionWhenCalledWithNull() {
        //given
        final var underTest = new VersionedSecretEntityId(HTTPS_LOCALHOST, SECRET_NAME_1, SECRET_VERSION_1);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.asRecoveryUri(null));

        //then + exception
    }

    @Test
    void testAsRecoveryUriShouldGenerateRecoveryUriWhenCalledBasedOnProvidedValues() {
        //given
        final var underTest = new VersionedSecretEntityId(HTTPS_LOCALHOST, SECRET_NAME_1, SECRET_VERSION_1);

        //when
        final var actual = underTest.asRecoveryUri(HTTPS_LOCALHOST_8443);

        //then
        Assertions.assertEquals(URI.create("https://localhost:8443/deletedsecrets/" + SECRET_NAME_1), actual);
    }

    @Test
    void testAsUriShouldAddQueryStringWhenCalledWithOne() {
        //given
        final var underTest = new VersionedSecretEntityId(HTTPS_LOCALHOST, SECRET_NAME_1, SECRET_VERSION_1);
        final var query = "?query=true";

        //when
        final var actual = underTest.asUri(HTTPS_LOCALHOST_8443, query);

        //then
        Assertions.assertEquals(URI.create("https://localhost:8443/secrets/" + SECRET_NAME_1 + "/" + SECRET_VERSION_1 + query), actual);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testAsUriShouldThrowExceptionWhenCalledWithNullQueryString() {
        //given
        final var underTest = new VersionedSecretEntityId(HTTPS_LOCALHOST, SECRET_NAME_1, SECRET_VERSION_1);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.asUri(HTTPS_LOCALHOST_8443, null));

        //then + exception;
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testAsUriShouldThrowExceptionWhenCalledWithNullWithBaseUri() {
        //given
        final var underTest = new VersionedSecretEntityId(HTTPS_LOCALHOST, SECRET_NAME_1, SECRET_VERSION_1);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.asUri(null, "?query"));

        //then + exception;
    }
}
