package com.github.nagyesta.lowkeyvault.service.key.impl;

import com.github.nagyesta.lowkeyvault.TestConstantsKeys;
import com.github.nagyesta.lowkeyvault.TestConstantsUri;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.service.exception.AlreadyExistsException;
import com.github.nagyesta.lowkeyvault.service.exception.CryptoException;
import com.github.nagyesta.lowkeyvault.service.vault.impl.VaultFakeImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigInteger;
import java.security.spec.RSAKeyGenParameterSpec;
import java.time.OffsetDateTime;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstants.TIME_10_MINUTES_AGO;
import static com.github.nagyesta.lowkeyvault.TestConstants.TIME_IN_10_MINUTES;

@Slf4j
@SuppressWarnings("checkstyle:MagicNumber")
class KeyVaultKeyEntityTest {

    public static Stream<Arguments> symmetricProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, 128))
                .add(Arguments.of(KeyType.OCT_HSM.getAlgorithmName(), 1))
                .build();
    }


    public static Stream<Arguments> asymmetricProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, 2048))
                .add(Arguments.of(KeyType.RSA.getAlgorithmName(), 0))
                .build();
    }

    public static Stream<Arguments> purgeExpirySource() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null, false))
                .add(Arguments.of(TIME_10_MINUTES_AGO, TIME_IN_10_MINUTES, false))
                .add(Arguments.of(TIME_IN_10_MINUTES, TIME_IN_10_MINUTES, false))
                .add(Arguments.of(TIME_10_MINUTES_AGO, null, false))
                .add(Arguments.of(TIME_IN_10_MINUTES, TIME_10_MINUTES_AGO, true))
                .add(Arguments.of(TIME_10_MINUTES_AGO, TIME_10_MINUTES_AGO, true))
                .add(Arguments.of(null, TIME_10_MINUTES_AGO, true))
                .build();
    }

    @ParameterizedTest
    @MethodSource("symmetricProvider")
    void testKeyGeneratorShouldCatchAndWrapExceptionsWhenTheyAreThrown(final String alg, final int size) {
        //given

        //when
        Assertions.assertThrows(CryptoException.class, () -> KeyVaultKeyEntity.keyGenerator(alg, size, log));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("asymmetricProvider")
    void testKeyPairGeneratorShouldCatchAndWrapExceptionsWhenTheyAreThrown(final String alg, final Integer size) {
        //given
        final RSAKeyGenParameterSpec rsaKeyGenParameterSpec = new RSAKeyGenParameterSpec(size, BigInteger.ONE);

        //when
        Assertions.assertThrows(CryptoException.class, () -> KeyVaultKeyEntity.keyPairGenerator(alg, rsaKeyGenParameterSpec, log));

        //then + exception
    }

    @Test
    void testDoCryptoShouldCatchAndWrapExceptionsWhenTheyAreThrown() {
        //given
        final RsaKeyVaultKeyEntity underTest = new RsaKeyVaultKeyEntity(TestConstantsKeys.VERSIONED_KEY_ENTITY_ID_1_VERSION_1,
                new VaultFakeImpl(TestConstantsUri.HTTPS_LOCALHOST_8443),
                2048, BigInteger.valueOf(3), false);

        //when
        Assertions.assertThrows(CryptoException.class, () -> underTest.doCrypto(() -> {
            throw new AlreadyExistsException("");
        }, "Message", log));
    }

    @ParameterizedTest
    @MethodSource("purgeExpirySource")
    void testIsPurgeExpiredShouldReturnTrueOnlyWhenCalledAfterTheDeadline(
            final OffsetDateTime deleted, final OffsetDateTime purgable, final boolean expected) {
        //given
        final RsaKeyVaultKeyEntity underTest = new RsaKeyVaultKeyEntity(TestConstantsKeys.VERSIONED_KEY_ENTITY_ID_1_VERSION_1,
                new VaultFakeImpl(TestConstantsUri.HTTPS_LOCALHOST_8443),
                2048, BigInteger.valueOf(3), false);
        underTest.setDeletedDate(deleted);
        underTest.setScheduledPurgeDate(purgable);

        //when
        final boolean actual = underTest.isPurgeExpired();

        //then
        Assertions.assertEquals(deleted, underTest.getDeletedDate().orElse(null));
        Assertions.assertEquals(purgable, underTest.getScheduledPurgeDate().orElse(null));
        Assertions.assertEquals(expected, actual);
    }
}
