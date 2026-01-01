package com.github.nagyesta.lowkeyvault.service;

import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static com.github.nagyesta.lowkeyvault.TestConstantsKeys.KEY_NAME_1;
import static com.github.nagyesta.lowkeyvault.TestConstantsKeys.KEY_VERSION_1;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOCALHOST;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOCALHOST_8443;

class VersionedKeyEntityIdTest {

    @Test
    void testConstructorShouldGenerateAVersionWhenNotProvidedAsParameter() {
        //given
        final var underTest = new VersionedKeyEntityId(HTTPS_LOCALHOST, KEY_NAME_1);

        //when
        final var actual = underTest.version();

        //then
        Assertions.assertNotNull(actual);
    }

    @Test
    void testConstructorShouldUseVersionWhenProvidedAsParameter() {
        //given
        final var underTest = new VersionedKeyEntityId(HTTPS_LOCALHOST, KEY_NAME_1, KEY_VERSION_1);

        //when
        final var actual = underTest.version();

        //then
        Assertions.assertEquals(KEY_VERSION_1, actual);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testAsUriNoversionShouldThrowExceptionWhenCalledWithNull() {
        //given
        final var underTest = new VersionedKeyEntityId(HTTPS_LOCALHOST, KEY_NAME_1, KEY_VERSION_1);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.asUriNoVersion(null));

        //then + exception
    }

    @Test
    void testAsUriShouldGenerateUriWhenCalledBasedOnProvidedValues() {
        //given
        final var underTest = new VersionedKeyEntityId(HTTPS_LOCALHOST, KEY_NAME_1, KEY_VERSION_1);

        //when
        final var actual = underTest.asUri(HTTPS_LOCALHOST_8443);

        //then
        Assertions.assertEquals(URI.create("https://localhost:8443/keys/" + KEY_NAME_1 + "/" + KEY_VERSION_1), actual);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testAsUriShouldThrowExceptionWhenCalledWithNull() {
        //given
        final var underTest = new VersionedKeyEntityId(HTTPS_LOCALHOST, KEY_NAME_1, KEY_VERSION_1);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.asUri(null));

        //then + exception
    }

    @Test
    void testAsRecoveryUriShouldGenerateRecoveryUriWhenCalledBasedOnProvidedValues() {
        //given
        final var underTest = new VersionedKeyEntityId(HTTPS_LOCALHOST, KEY_NAME_1, KEY_VERSION_1);

        //when
        final var actual = underTest.asRecoveryUri(HTTPS_LOCALHOST_8443);

        //then
        Assertions.assertEquals(URI.create("https://localhost:8443/deletedkeys/" + KEY_NAME_1), actual);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testAsRecoveryUriShouldThrowExceptionWhenCalledWithNull() {
        //given
        final var underTest = new VersionedKeyEntityId(HTTPS_LOCALHOST, KEY_NAME_1, KEY_VERSION_1);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.asRecoveryUri(null));

        //then + exception
    }

    @Test
    void testAsUriShouldAddQueryStringWhenCalledWithOne() {
        //given
        final var underTest = new VersionedKeyEntityId(HTTPS_LOCALHOST, KEY_NAME_1, KEY_VERSION_1);
        final var query = "?query=true";

        //when
        final var actual = underTest.asUri(HTTPS_LOCALHOST_8443, query);

        //then
        Assertions.assertEquals(URI.create("https://localhost:8443/keys/" + KEY_NAME_1 + "/" + KEY_VERSION_1 + query), actual);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testAsUriShouldThrowExceptionWhenCalledWithNullQueryString() {
        //given
        final var underTest = new VersionedKeyEntityId(HTTPS_LOCALHOST, KEY_NAME_1, KEY_VERSION_1);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.asUri(HTTPS_LOCALHOST_8443, null));

        //then + exception
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testAsUriShouldThrowExceptionWhenCalledWithNullWithBaseUri() {
        //given
        final var underTest = new VersionedKeyEntityId(HTTPS_LOCALHOST, KEY_NAME_1, KEY_VERSION_1);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.asUri(null, "?query"));

        //then + exception
    }
}
