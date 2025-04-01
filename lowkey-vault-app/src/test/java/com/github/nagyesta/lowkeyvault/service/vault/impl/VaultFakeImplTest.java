package com.github.nagyesta.lowkeyvault.service.vault.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstants.NUMBER_OF_SECONDS_IN_10_MINUTES;
import static com.github.nagyesta.lowkeyvault.TestConstants.TOMCAT_SECURE_PORT;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.*;
import static com.github.nagyesta.lowkeyvault.context.util.VaultUriUtil.replacePortWith;

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
        final UnaryOperator<URI> identity = uri -> uri;
        return Stream.<Arguments>builder()
                .add(Arguments.of(HTTPS_LOCALHOST, HTTPS_LOCALHOST_80, identity))
                .add(Arguments.of(HTTPS_LOCALHOST, HTTPS_LOCALHOST_8443, identity))
                .add(Arguments.of(HTTPS_LOCALHOST_80, HTTPS_LOCALHOST_8443, identity))
                .add(Arguments.of(HTTPS_LOCALHOST, HTTPS_LOCALHOST, identity))
                .add(Arguments.of(HTTPS_LOWKEY_VAULT_8443, HTTPS_LOWKEY_VAULT_443,
                        (UnaryOperator<URI>) uri -> replacePortWith(uri, TOMCAT_SECURE_PORT)))
                .add(Arguments.of(HTTPS_LOWKEY_VAULT_443, HTTPS_LOWKEY_VAULT_8443,
                        (UnaryOperator<URI>) uri -> replacePortWith(uri, TOMCAT_SECURE_PORT)))
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
    void testMatchesShouldUseFullMatchWhenCalled(final URI self, final URI input, final UnaryOperator<URI> uriMapper) {
        //given
        final var underTest = new VaultFakeImpl(self);

        //when
        final var actual = underTest.matches(input, uriMapper);

        //then
        final var expected = uriMapper.apply(self).equals(uriMapper.apply(input));
        Assertions.assertEquals(expected, actual, "URI was expected to match: " + self);
    }

    @ParameterizedTest
    @MethodSource("uriPairProvider")
    void testMatchesShouldUseFullMatchWithAnyOfTheAliasesWhenCalled(final URI self, final URI input, final UnaryOperator<URI> uriMapper) {
        //given
        final var underTest = new VaultFakeImpl(HTTPS_AZURE_CLOUD);
        underTest.setAliases(Set.of(self, HTTPS_DEFAULT_LOWKEY_VAULT));

        //when
        final var actual = underTest.matches(input, uriMapper);

        //then
        final var expected = uriMapper.apply(self).equals(uriMapper.apply(input));
        Assertions.assertEquals(expected, actual, "URI was expected to match alias: " + self);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testMatchesShouldThrowExceptionWhenCalledWithNullUri() {
        //given
        final var underTest = new VaultFakeImpl(HTTPS_LOCALHOST);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.matches(null, uri -> uri));

        //then + exception
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testMatchesShouldThrowExceptionWhenCalledWithNullMapper() {
        //given
        final var underTest = new VaultFakeImpl(HTTPS_LOCALHOST);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.matches(HTTPS_LOCALHOST, null));

        //then + exception
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testSetAliasesShouldThrowExceptionWhenCalledWithNull() {
        //given
        final var underTest = new VaultFakeImpl(HTTPS_LOCALHOST);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.setAliases(null));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("uriProvider")
    void testBaseUriShouldReturnUriPassedToConstructorWhenCalled(final URI self) {
        //given
        final var underTest = new VaultFakeImpl(self);

        //when
        final var actual = underTest.baseUri();

        //then
        Assertions.assertEquals(self, actual);
    }

    @ParameterizedTest
    @MethodSource("uriProvider")
    void testAliasesShouldReturnNewSetOfUrisPassedToSetAliasesWhenCalled(final URI self) {
        //given
        final var underTest = new VaultFakeImpl(HTTPS_DEFAULT_LOWKEY_VAULT);
        final Set<URI> expected = new TreeSet<>();
        expected.add(self);
        underTest.setAliases(expected);

        //when
        final var actual = underTest.aliases();

        //then
        Assertions.assertIterableEquals(expected, actual);
        Assertions.assertNotSame(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("uriProvider")
    void testSetAliasesShouldThrowExceptionWhenBaseUriIsInTheAliasSet(final URI self) {
        //given
        final var underTest = new VaultFakeImpl(self);
        final var expected = Set.of(self);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.setAliases(expected));

        //then + exception
    }

    @Test
    void testKeyVaultFakeShouldNeverBeNullWhenCalled() {
        //given
        final var underTest = new VaultFakeImpl(HTTPS_LOCALHOST);

        //when
        final var actual = underTest.keyVaultFake();

        //then
        Assertions.assertNotNull(actual);
    }

    @Test
    void testSecretVaultFakeShouldNeverBeNullWhenCalled() {
        //given
        final var underTest = new VaultFakeImpl(HTTPS_LOCALHOST);

        //when
        final var actual = underTest.secretVaultFake();

        //then
        Assertions.assertNotNull(actual);
    }

    @Test
    void testCertificateVaultFakeShouldNeverBeNullWhenCalled() {
        //given
        final var underTest = new VaultFakeImpl(HTTPS_LOCALHOST);

        //when
        final var actual = underTest.certificateVaultFake();

        //then
        Assertions.assertNotNull(actual);
    }

    @Test
    void testGetRecoveryLevelShouldReturnRecoverableWhenCalledByDefault() {
        //given
        final var underTest = new VaultFakeImpl(HTTPS_LOCALHOST);

        //when
        final var actual = underTest.getRecoveryLevel();

        //then
        Assertions.assertEquals(RecoveryLevel.RECOVERABLE, actual);
    }

    @Test
    void testGetRecoverableDaysShouldReturn90WhenCalledByDefault() {
        //given
        final var underTest = new VaultFakeImpl(HTTPS_LOCALHOST);

        //when
        final var actual = underTest.getRecoverableDays();

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
        final var underTest = new VaultFakeImpl(HTTPS_LOCALHOST,
                RecoveryLevel.RECOVERABLE_AND_PROTECTED_SUBSCRIPTION, RecoveryLevel.MAX_RECOVERABLE_DAYS_INCLUSIVE);

        //when
        Assertions.assertThrows(IllegalStateException.class, underTest::delete);

        //then + exception
    }

    @Test
    void testGetDeletedOnShouldBeNullWhenNotDeleted() {
        //given
        final var underTest = new VaultFakeImpl(HTTPS_LOCALHOST,
                RecoveryLevel.RECOVERABLE, RecoveryLevel.MAX_RECOVERABLE_DAYS_INCLUSIVE);

        //when
        final var deletedOnBeforeDeletion = underTest.getDeletedOn();
        underTest.delete();
        final var deletedOnWhileDeleted = underTest.getDeletedOn();
        underTest.recover();
        final var deletedOnAfterRecovery = underTest.getDeletedOn();

        //then
        Assertions.assertNull(deletedOnBeforeDeletion);
        Assertions.assertNotNull(deletedOnWhileDeleted);
        Assertions.assertNull(deletedOnAfterRecovery);
    }

    @SuppressWarnings("java:S2925") //this is a fixed duration wait to let the timestamps change
    @Test
    void testGetCreatedOnShouldBeInThePastWhenCalled() {
        //given
        final var underTest = new VaultFakeImpl(HTTPS_LOCALHOST,
                RecoveryLevel.RECOVERABLE_AND_PROTECTED_SUBSCRIPTION, RecoveryLevel.MAX_RECOVERABLE_DAYS_INCLUSIVE);
        Assertions.assertDoesNotThrow(() -> Thread.sleep(WAIT_MILLIS));
        //when
        final var actual = underTest.getCreatedOn();

        //then
        Assertions.assertTrue(actual.isBefore(OffsetDateTime.now()));
    }

    @ParameterizedTest
    @ValueSource(ints = {-42, -10, -5, -3, -2, -1, 0})
    void testTimeShiftShouldThrowExceptionWhenCalledWithNegativeOrZero(final int value) {
        //given
        final var underTest = new VaultFakeImpl(HTTPS_LOCALHOST,
                RecoveryLevel.RECOVERABLE_AND_PROTECTED_SUBSCRIPTION, RecoveryLevel.MAX_RECOVERABLE_DAYS_INCLUSIVE);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.timeShift(value, false));

        //then + exception
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @Test
    void testTimeShiftShouldReduceTimeStampsWhenCalledWithPositive() {
        //given
        final var underTest = new VaultFakeImpl(HTTPS_LOCALHOST,
                RecoveryLevel.RECOVERABLE_AND_PURGEABLE, RecoveryLevel.MAX_RECOVERABLE_DAYS_INCLUSIVE);
        underTest.delete();
        final var createdOriginal = underTest.getCreatedOn();
        final var deletedOriginal = underTest.getDeletedOn();

        //when
        underTest.timeShift(NUMBER_OF_SECONDS_IN_10_MINUTES, false);

        //then
        Assertions.assertEquals(createdOriginal.minusSeconds(NUMBER_OF_SECONDS_IN_10_MINUTES), underTest.getCreatedOn());
        Assertions.assertEquals(deletedOriginal.minusSeconds(NUMBER_OF_SECONDS_IN_10_MINUTES), underTest.getDeletedOn());
    }
}
