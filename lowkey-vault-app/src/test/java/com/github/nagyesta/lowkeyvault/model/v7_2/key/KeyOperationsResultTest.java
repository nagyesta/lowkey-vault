package com.github.nagyesta.lowkeyvault.model.v7_2.key;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.EncryptionAlgorithm;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.KeyOperationsParameters;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstants.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsKeys.VERSIONED_KEY_ENTITY_ID_1_VERSION_1;

class KeyOperationsResultTest {

    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

    public static Stream<Arguments> validStringProvider() {
        final var fullOps = new KeyOperationsParameters();
        fullOps.setAlgorithm(EncryptionAlgorithm.A128CBC);
        fullOps.setAdditionalAuthData(BLANK.getBytes(StandardCharsets.UTF_8));
        fullOps.setAuthenticationTag(DEFAULT_VAULT.getBytes(StandardCharsets.UTF_8));
        fullOps.setInitializationVector(LOCALHOST.getBytes(StandardCharsets.UTF_8));
        return Stream.<Arguments>builder()
                .add(Arguments.of(VERSIONED_KEY_ENTITY_ID_1_VERSION_1, EMPTY, fullOps))
                .add(Arguments.of(VERSIONED_KEY_ENTITY_ID_1_VERSION_1, BLANK, fullOps))
                .add(Arguments.of(VERSIONED_KEY_ENTITY_ID_1_VERSION_1, DEFAULT_VAULT, fullOps))
                .add(Arguments.of(VERSIONED_KEY_ENTITY_ID_1_VERSION_1, LOCALHOST, new KeyOperationsParameters()))
                .build();
    }

    @ParameterizedTest
    @MethodSource("validStringProvider")
    void testForBytesShouldImplicitlyEncodeBytesAsBase64WhenCalledWitValidInput(
            final VersionedKeyEntityId id,
            final String value,
            final KeyOperationsParameters params) {
        //given
        final var bytes = value.getBytes(StandardCharsets.UTF_8);

        //when
        final var actual = KeyOperationsResult.forBytes(id, bytes, params, id.vault());

        //then
        assertResultMatches(id, bytes, params, actual);
    }

    @ParameterizedTest
    @MethodSource("validStringProvider")
    void testForBytesShouldKeepValueAsIsWhenCalledWitValidInput(
            final VersionedKeyEntityId id,
            final String value,
            final KeyOperationsParameters params) {
        //given
        final var encoded = ENCODER.encode(value.getBytes(StandardCharsets.UTF_8));

        //when
        final var actual = KeyOperationsResult.forBytes(id, encoded, params, id.vault());

        //then
        assertResultMatches(id, encoded, params, actual);
    }

    private void assertResultMatches(
            final VersionedKeyEntityId id,
            final byte[] encoded,
            final KeyOperationsParameters params,
            final KeyOperationsResult actual) {
        Assertions.assertArrayEquals(encoded, actual.getValue());
        Assertions.assertEquals(id.asUri(id.vault()), actual.getId());
        Assertions.assertEquals(params.getInitializationVector(), actual.getInitializationVector());
        Assertions.assertEquals(params.getAdditionalAuthData(), actual.getAdditionalAuthData());
        Assertions.assertEquals(params.getAuthenticationTag(), actual.getAuthenticationTag());
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void testForBytesShouldThrowExceptionWhenCalledWitInvalidInput() {
        //given
        final var value = BLANK.getBytes(StandardCharsets.UTF_8);
        final var params = new KeyOperationsParameters();

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> KeyOperationsResult.forBytes(VERSIONED_KEY_ENTITY_ID_1_VERSION_1, value, params, null));

        //then exception
    }
}
