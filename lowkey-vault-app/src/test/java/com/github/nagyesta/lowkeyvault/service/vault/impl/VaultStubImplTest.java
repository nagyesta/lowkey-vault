package com.github.nagyesta.lowkeyvault.service.vault.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateVaultStub;
import com.github.nagyesta.lowkeyvault.service.key.KeyVaultStub;
import com.github.nagyesta.lowkeyvault.service.secret.SecretVaultStub;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstantsUri.*;

class VaultStubImplTest {

    public static Stream<Arguments> uriProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(HTTPS_LOCALHOST))
                .add(Arguments.of(HTTPS_LOCALHOST_80))
                .add(Arguments.of(HTTPS_LOCALHOST_8443))
                .add(Arguments.of(HTTPS_LOWKEY_VAULT_8443))
                .build();
    }

    public static Stream<Arguments> uriPairProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(HTTPS_LOCALHOST, HTTPS_LOCALHOST_80))
                .add(Arguments.of(HTTPS_LOCALHOST, HTTPS_LOCALHOST_8443))
                .add(Arguments.of(HTTPS_LOCALHOST_80, HTTPS_LOCALHOST_8443))
                .add(Arguments.of(HTTPS_LOCALHOST, HTTPS_LOCALHOST))
                .build();
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    public static Stream<Arguments> invalidRecoveryParameterProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(RecoveryLevel.RECOVERABLE, null))
                .add(Arguments.of(RecoveryLevel.RECOVERABLE, 42))
                .add(Arguments.of(RecoveryLevel.PURGEABLE, 21))
                .add(Arguments.of(RecoveryLevel.CUSTOMIZED_RECOVERABLE, 5))
                .add(Arguments.of(null, 5))
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testConstructorShouldThrowExceptionWhenCalledWithNull() {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> new VaultStubImpl(null));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("uriPairProvider")
    void testMatchesShouldUseFullMatchWhenCalled(final URI self, final URI other) {
        //given
        final VaultStubImpl underTest = new VaultStubImpl(self);

        //when
        final boolean actual = underTest.matches(other);

        //then
        Assertions.assertEquals(self.equals(other), actual);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testMatchesShouldThrowExceptionWhenCalledWithNull() {
        //given
        final VaultStubImpl underTest = new VaultStubImpl(HTTPS_LOCALHOST);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.matches(null));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("uriProvider")
    void testBaseUriShouldReturnUriPassedToConstructorWhenCalled(final URI self) {
        //given
        final VaultStubImpl underTest = new VaultStubImpl(self);

        //when
        final URI actual = underTest.baseUri();

        //then
        Assertions.assertEquals(self, actual);
    }

    @Test
    void testKeyVaultStubShouldNeverBeNullWhenCalled() {
        //given
        final VaultStubImpl underTest = new VaultStubImpl(HTTPS_LOCALHOST);

        //when
        final KeyVaultStub actual = underTest.keyVaultStub();

        //then
        Assertions.assertNotNull(actual);
    }

    @Test
    void testSecretVaultStubShouldNeverBeNullWhenCalled() {
        //given
        final VaultStubImpl underTest = new VaultStubImpl(HTTPS_LOCALHOST);

        //when
        final SecretVaultStub actual = underTest.secretVaultStub();

        //then
        Assertions.assertNotNull(actual);
    }

    @Test
    void testCertificateVaultStubShouldNeverBeNullWhenCalled() {
        //given
        final VaultStubImpl underTest = new VaultStubImpl(HTTPS_LOCALHOST);

        //when
        final CertificateVaultStub actual = underTest.certificateVaultStub();

        //then
        Assertions.assertNotNull(actual);
    }

    @Test
    void testGetRecoveryLevelShouldReturnNullWhenCalledByDefault() {
        //given
        final VaultStubImpl underTest = new VaultStubImpl(HTTPS_LOCALHOST);

        //when
        final RecoveryLevel actual = underTest.getRecoveryLevel();

        //then
        Assertions.assertNull(actual);
    }

    @Test
    void testGetRecoverableDaysShouldReturnNullWhenCalledByDefault() {
        //given
        final VaultStubImpl underTest = new VaultStubImpl(HTTPS_LOCALHOST);

        //when
        final Integer actual = underTest.getRecoverableDays();

        //then
        Assertions.assertNull(actual);
    }

    @ParameterizedTest
    @MethodSource("invalidRecoveryParameterProvider")
    void testSetDefaultRecoveryShouldValidateThePairOfParametersWhenCalled(
            final RecoveryLevel recoveryLevel, final Integer days) {
        //given
        final VaultStubImpl underTest = new VaultStubImpl(HTTPS_LOCALHOST);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.setDefaultRecovery(recoveryLevel, days));

        //then + exception
    }

    @Test
    void testRecoveryLevelAndRecoverableDaysShouldBeReturnedWhenSetBefore() {
        //given
        final RecoveryLevel recoveryLevel = RecoveryLevel.RECOVERABLE;
        final int recoverableDays = 90;
        final VaultStubImpl underTest = new VaultStubImpl(HTTPS_LOCALHOST);
        underTest.setDefaultRecovery(recoveryLevel, recoverableDays);

        //when
        final Integer actualDays = underTest.getRecoverableDays();
        final RecoveryLevel actualLevel = underTest.getRecoveryLevel();

        //then
        Assertions.assertEquals(recoverableDays, actualDays);
        Assertions.assertEquals(recoveryLevel, actualLevel);
    }
}
