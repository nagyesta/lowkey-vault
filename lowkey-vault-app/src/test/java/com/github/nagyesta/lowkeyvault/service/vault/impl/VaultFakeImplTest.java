package com.github.nagyesta.lowkeyvault.service.vault.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateVaultFake;
import com.github.nagyesta.lowkeyvault.service.key.KeyVaultFake;
import com.github.nagyesta.lowkeyvault.service.secret.SecretVaultFake;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstants.NUMBER_OF_SECONDS_IN_10_MINUTES;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.*;

class VaultFakeImplTest {

    private static final int WAIT_MILLIS = 5;

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
                .add(Arguments.of(RecoveryLevel.CUSTOMIZED_RECOVERABLE, WAIT_MILLIS))
                .add(Arguments.of(null, WAIT_MILLIS))
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testConstructorShouldThrowExceptionWhenCalledWithNull() {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> new VaultFakeImpl(null));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("uriPairProvider")
    void testMatchesShouldUseFullMatchWhenCalled(final URI self, final URI other) {
        //given
        final VaultFakeImpl underTest = new VaultFakeImpl(self);

        //when
        final boolean actual = underTest.matches(other);

        //then
        Assertions.assertEquals(self.equals(other), actual);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testMatchesShouldThrowExceptionWhenCalledWithNull() {
        //given
        final VaultFakeImpl underTest = new VaultFakeImpl(HTTPS_LOCALHOST);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.matches(null));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("uriProvider")
    void testBaseUriShouldReturnUriPassedToConstructorWhenCalled(final URI self) {
        //given
        final VaultFakeImpl underTest = new VaultFakeImpl(self);

        //when
        final URI actual = underTest.baseUri();

        //then
        Assertions.assertEquals(self, actual);
    }

    @Test
    void testKeyVaultFakeShouldNeverBeNullWhenCalled() {
        //given
        final VaultFakeImpl underTest = new VaultFakeImpl(HTTPS_LOCALHOST);

        //when
        final KeyVaultFake actual = underTest.keyVaultFake();

        //then
        Assertions.assertNotNull(actual);
    }

    @Test
    void testSecretVaultFakeShouldNeverBeNullWhenCalled() {
        //given
        final VaultFakeImpl underTest = new VaultFakeImpl(HTTPS_LOCALHOST);

        //when
        final SecretVaultFake actual = underTest.secretVaultFake();

        //then
        Assertions.assertNotNull(actual);
    }

    @Test
    void testCertificateVaultFakeShouldNeverBeNullWhenCalled() {
        //given
        final VaultFakeImpl underTest = new VaultFakeImpl(HTTPS_LOCALHOST);

        //when
        final CertificateVaultFake actual = underTest.certificateVaultFake();

        //then
        Assertions.assertNotNull(actual);
    }

    @Test
    void testGetRecoveryLevelShouldReturnRecoverableWhenCalledByDefault() {
        //given
        final VaultFakeImpl underTest = new VaultFakeImpl(HTTPS_LOCALHOST);

        //when
        final RecoveryLevel actual = underTest.getRecoveryLevel();

        //then
        Assertions.assertEquals(RecoveryLevel.RECOVERABLE, actual);
    }

    @Test
    void testGetRecoverableDaysShouldReturn90WhenCalledByDefault() {
        //given
        final VaultFakeImpl underTest = new VaultFakeImpl(HTTPS_LOCALHOST);

        //when
        final Integer actual = underTest.getRecoverableDays();

        //then
        Assertions.assertEquals(RecoveryLevel.MAX_RECOVERABLE_DAYS_INCLUSIVE, actual);
    }

    @ParameterizedTest
    @MethodSource("invalidRecoveryParameterProvider")
    void testConstructorWithRecoveryShouldValidateThePairOfParametersWhenCalled(
            final RecoveryLevel recoveryLevel, final Integer days) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> new VaultFakeImpl(HTTPS_LOCALHOST, recoveryLevel, days));

        //then + exception
    }

    @Test
    void testDeleteShouldThrowExceptionWhenVaultIsSubscriptionProtected() {
        //given
        final VaultFakeImpl underTest = new VaultFakeImpl(HTTPS_LOCALHOST,
                RecoveryLevel.RECOVERABLE_AND_PROTECTED_SUBSCRIPTION, RecoveryLevel.MAX_RECOVERABLE_DAYS_INCLUSIVE);

        //when
        Assertions.assertThrows(IllegalStateException.class, underTest::delete);

        //then + exception
    }

    @Test
    void testGetDeletedOnShouldBeNullWhenNotDeleted() {
        //given
        final VaultFakeImpl underTest = new VaultFakeImpl(HTTPS_LOCALHOST,
                RecoveryLevel.RECOVERABLE, RecoveryLevel.MAX_RECOVERABLE_DAYS_INCLUSIVE);

        //when
        final OffsetDateTime deletedOnBeforeDeletion = underTest.getDeletedOn();
        underTest.delete();
        final OffsetDateTime deletedOnWhileDeleted = underTest.getDeletedOn();
        underTest.recover();
        final OffsetDateTime deletedOnAfterRecovery = underTest.getDeletedOn();

        //then
        Assertions.assertNull(deletedOnBeforeDeletion);
        Assertions.assertNotNull(deletedOnWhileDeleted);
        Assertions.assertNull(deletedOnAfterRecovery);
    }

    @Test
    void testGetCreatedOnShouldBeInThePastWhenCalled() {
        //given
        final VaultFakeImpl underTest = new VaultFakeImpl(HTTPS_LOCALHOST,
                RecoveryLevel.RECOVERABLE_AND_PROTECTED_SUBSCRIPTION, RecoveryLevel.MAX_RECOVERABLE_DAYS_INCLUSIVE);
        Assertions.assertDoesNotThrow(() -> Thread.sleep(WAIT_MILLIS));
        //when
        final OffsetDateTime actual = underTest.getCreatedOn();

        //then
        Assertions.assertTrue(actual.isBefore(OffsetDateTime.now()));
    }

    @ParameterizedTest
    @ValueSource(ints = {-42, -10, -5, -3, -2, -1, 0})
    void testTimeShiftShouldThrowExceptionWhenCalledWithNegativeOrZero(final int value) {
        //given
        final VaultFakeImpl underTest = new VaultFakeImpl(HTTPS_LOCALHOST,
                RecoveryLevel.RECOVERABLE_AND_PROTECTED_SUBSCRIPTION, RecoveryLevel.MAX_RECOVERABLE_DAYS_INCLUSIVE);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.timeShift(value));

        //then + exception
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @Test
    void testTimeShiftShouldReduceTimeStampsWhenCalledWithPositive() {
        //given
        final VaultFakeImpl underTest = new VaultFakeImpl(HTTPS_LOCALHOST,
                RecoveryLevel.RECOVERABLE_AND_PURGEABLE, RecoveryLevel.MAX_RECOVERABLE_DAYS_INCLUSIVE);
        underTest.delete();
        final OffsetDateTime createdOriginal = underTest.getCreatedOn();
        final OffsetDateTime deletedOriginal = underTest.getDeletedOn();

        //when
        underTest.timeShift(NUMBER_OF_SECONDS_IN_10_MINUTES);

        //then
        Assertions.assertEquals(createdOriginal.minusSeconds(NUMBER_OF_SECONDS_IN_10_MINUTES), underTest.getCreatedOn());
        Assertions.assertEquals(deletedOriginal.minusSeconds(NUMBER_OF_SECONDS_IN_10_MINUTES), underTest.getDeletedOn());
    }
}
