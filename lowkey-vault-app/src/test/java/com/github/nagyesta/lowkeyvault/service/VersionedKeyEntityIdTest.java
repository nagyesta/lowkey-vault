package com.github.nagyesta.lowkeyvault.service;

import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstantsKeys.KEY_NAME_1;
import static com.github.nagyesta.lowkeyvault.TestConstantsKeys.KEY_VERSION_1;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOCALHOST;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOCALHOST_8443;

class VersionedKeyEntityIdTest {

    public static Stream<Arguments> invalidMinimalParameterProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null))
                .add(Arguments.of(HTTPS_LOCALHOST, null))
                .add(Arguments.of(null, KEY_NAME_1))
                .build();
    }

    public static Stream<Arguments> invalidParameterProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, KEY_NAME_1, KEY_VERSION_1))
                .add(Arguments.of(HTTPS_LOCALHOST, null, KEY_VERSION_1))
                .add(Arguments.of(HTTPS_LOCALHOST, KEY_NAME_1, null))
                .build();
    }

    @Test
    void testConstructorShouldGenerateAVersionWhenNotProvidedAsParameter() {
        //given
        final VersionedKeyEntityId underTest = new VersionedKeyEntityId(HTTPS_LOCALHOST, KEY_NAME_1);

        //when
        final String actual = underTest.version();

        //then
        Assertions.assertNotNull(actual);
    }

    @Test
    void testConstructorShouldUseVersionWhenProvidedAsParameter() {
        //given
        final VersionedKeyEntityId underTest = new VersionedKeyEntityId(HTTPS_LOCALHOST, KEY_NAME_1, KEY_VERSION_1);

        //when
        final String actual = underTest.version();

        //then
        Assertions.assertEquals(KEY_VERSION_1, actual);
    }

    @ParameterizedTest
    @MethodSource("invalidParameterProvider")
    void testConstructorShouldThrowExceptionWhenCalledWithNulls(
            final URI vault, final String id, final String version) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> new VersionedKeyEntityId(vault, id, version));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("invalidMinimalParameterProvider")
    void testConstructorShouldThrowExceptionWhenCalledWithNulls(
            final URI vault, final String id) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> new VersionedKeyEntityId(vault, id));

        //then + exception
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testAsRotationPolicyUriShouldThrowExceptionWhenCalledWithNull() {
        //given
        final VersionedKeyEntityId underTest = new VersionedKeyEntityId(HTTPS_LOCALHOST, KEY_NAME_1, KEY_VERSION_1);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.asRotationPolicyUri(null));

        //then + exception
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testAsUriNoversionShouldThrowExceptionWhenCalledWithNull() {
        //given
        final VersionedKeyEntityId underTest = new VersionedKeyEntityId(HTTPS_LOCALHOST, KEY_NAME_1, KEY_VERSION_1);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.asUriNoVersion(null));

        //then + exception
    }

    @Test
    void testAsUriShouldGenerateUriWhenCalledBasedOnProvidedValues() {
        //given
        final VersionedKeyEntityId underTest = new VersionedKeyEntityId(HTTPS_LOCALHOST, KEY_NAME_1, KEY_VERSION_1);

        //when
        final URI actual = underTest.asUri(HTTPS_LOCALHOST_8443);

        //then
        Assertions.assertEquals(URI.create("https://localhost:8443/keys/" + KEY_NAME_1 + "/" + KEY_VERSION_1), actual);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testAsUriShouldThrowExceptionWhenCalledWithNull() {
        //given
        final VersionedKeyEntityId underTest = new VersionedKeyEntityId(HTTPS_LOCALHOST, KEY_NAME_1, KEY_VERSION_1);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.asUri(null));

        //then + exception
    }

    @Test
    void testAsRecoveryUriShouldGenerateRecoveryUriWhenCalledBasedOnProvidedValues() {
        //given
        final VersionedKeyEntityId underTest = new VersionedKeyEntityId(HTTPS_LOCALHOST, KEY_NAME_1, KEY_VERSION_1);

        //when
        final URI actual = underTest.asRecoveryUri(HTTPS_LOCALHOST_8443);

        //then
        Assertions.assertEquals(URI.create("https://localhost:8443/deletedkeys/" + KEY_NAME_1), actual);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testAsRecoveryUriShouldThrowExceptionWhenCalledWithNull() {
        //given
        final VersionedKeyEntityId underTest = new VersionedKeyEntityId(HTTPS_LOCALHOST, KEY_NAME_1, KEY_VERSION_1);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.asRecoveryUri(null));

        //then + exception
    }

    @Test
    void testAsUriShouldAddQueryStringWhenCalledWithOne() {
        //given
        final VersionedKeyEntityId underTest = new VersionedKeyEntityId(HTTPS_LOCALHOST, KEY_NAME_1, KEY_VERSION_1);
        final String query = "?query=true";

        //when
        final URI actual = underTest.asUri(HTTPS_LOCALHOST_8443, query);

        //then
        Assertions.assertEquals(URI.create("https://localhost:8443/keys/" + KEY_NAME_1 + "/" + KEY_VERSION_1 + query), actual);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testAsUriShouldThrowExceptionWhenCalledWithNullQueryString() {
        //given
        final VersionedKeyEntityId underTest = new VersionedKeyEntityId(HTTPS_LOCALHOST, KEY_NAME_1, KEY_VERSION_1);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.asUri(HTTPS_LOCALHOST_8443, (String) null));

        //then + exception;
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testAsUriShouldThrowExceptionWhenCalledWithNullWithBaseUri() {
        //given
        final VersionedKeyEntityId underTest = new VersionedKeyEntityId(HTTPS_LOCALHOST, KEY_NAME_1, KEY_VERSION_1);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.asUri(null, "?query"));

        //then + exception;
    }
}
