package com.github.nagyesta.lowkeyvault.model.v7_2.key;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.EncryptionAlgorithm;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.KeyOperationsParameters;
import com.github.nagyesta.lowkeyvault.service.VersionedKeyEntityId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstants.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsKeys.VERSIONED_KEY_ENTITY_ID_1_VERSION_1;

class KeyOperationsResultTest {

    public static Stream<Arguments> validStringProvider() {
        final KeyOperationsParameters fullOps = new KeyOperationsParameters();
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

    public static Stream<Arguments> invalidBytesProvider() {
        return invalidStringProvider()
                .map(a -> {
                    final Object[] args = a.get();
                    final byte[] bytes = Optional.ofNullable(args[1])
                            .map(String.class::cast)
                            .map(s -> s.getBytes(StandardCharsets.UTF_8))
                            .orElse(null);
                    return Arguments.of(args[0], bytes, args[2]);
                });
    }

    public static Stream<Arguments> invalidStringProvider() {
        final KeyOperationsParameters parameters = new KeyOperationsParameters();
        final VersionedKeyEntityId keyId = VERSIONED_KEY_ENTITY_ID_1_VERSION_1;
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null, null))
                .add(Arguments.of(keyId, null, null))
                .add(Arguments.of(null, BLANK, null))
                .add(Arguments.of(null, null, parameters))
                .add(Arguments.of(null, BLANK, parameters))
                .add(Arguments.of(keyId, null, parameters))
                .add(Arguments.of(keyId, BLANK, null))
                .build();
    }

    @ParameterizedTest
    @MethodSource("validStringProvider")
    void testForBytesShouldImplicitlyEncodeBytesAsBase64WhenCalledWitValidInput(
            final VersionedKeyEntityId id, final String value, final KeyOperationsParameters params) {
        //given
        final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        final byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        final String encoded = encoder.encodeToString(bytes);

        //when
        final KeyOperationsResult actual = KeyOperationsResult.forBytes(id, bytes, params);

        //then
        assertResultMatches(id, encoded, params, actual);
    }

    @ParameterizedTest
    @MethodSource("validStringProvider")
    void testForStringShouldKeepValueAsIsWhenCalledWitValidInput(
            final VersionedKeyEntityId id, final String value, final KeyOperationsParameters params) {
        //given

        //when
        final KeyOperationsResult actual = KeyOperationsResult.forString(id, value, params);

        //then
        assertResultMatches(id, value, params, actual);
    }

    private void assertResultMatches(final VersionedKeyEntityId id, final String value,
                                     final KeyOperationsParameters params, final KeyOperationsResult actual) {
        Assertions.assertEquals(value, actual.getValue());
        Assertions.assertEquals(id.asUri(), actual.getId());
        Assertions.assertEquals(params.getInitializationVector(), actual.getInitializationVector());
        Assertions.assertEquals(params.getAdditionalAuthData(), actual.getAdditionalAuthData());
        Assertions.assertEquals(params.getAuthenticationTag(), actual.getAuthenticationTag());
    }

    @ParameterizedTest
    @MethodSource("invalidBytesProvider")
    void testForBytesShouldThrowExceptionWhenCalledWitInvalidInput(
            final VersionedKeyEntityId id, final byte[] value, final KeyOperationsParameters params
    ) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> KeyOperationsResult.forBytes(id, value, params));

        //then exception
    }

    @ParameterizedTest
    @MethodSource("invalidStringProvider")
    void testForStringShouldThrowExceptionWhenCalledWitInvalidInput(
            final VersionedKeyEntityId id, final String value, final KeyOperationsParameters params
    ) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> KeyOperationsResult.forString(id, value, params));

        //then exception
    }
}
