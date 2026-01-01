package com.github.nagyesta.lowkeyvault.service.vault.impl;

import com.github.nagyesta.lowkeyvault.TestConstants;
import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.service.exception.AlreadyExistsException;
import com.github.nagyesta.lowkeyvault.service.exception.NotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URI;
import java.util.*;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstantsUri.*;

class VaultServiceImplTest {

    private static final int WAIT_MILLIS = 2;

    public static Stream<Arguments> valueProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(Collections.singletonList(HTTPS_LOCALHOST_8443), HTTPS_LOCALHOST_8443))
                .add(Arguments.of(List.of(HTTPS_LOCALHOST, HTTPS_LOCALHOST_80, HTTPS_LOCALHOST_8443), HTTPS_LOCALHOST_8443))
                .build();
    }

    public static Stream<Arguments> valueMapProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(Collections.singletonMap(HTTPS_LOCALHOST_8443, false)))
                .add(Arguments.of(Collections.singletonMap(HTTPS_LOCALHOST_8443, true)))
                .add(Arguments.of(Map.of(
                        HTTPS_LOCALHOST, true,
                        HTTPS_LOCALHOST_80, false,
                        HTTPS_LOCALHOST_8443, true)))
                .build();
    }

    public static Stream<Arguments> missingValueProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(Collections.emptyList(), null))
                .add(Arguments.of(Collections.emptyList(), HTTPS_LOCALHOST))
                .add(Arguments.of(Collections.singletonList(HTTPS_LOCALHOST_80), HTTPS_LOCALHOST))
                .add(Arguments.of(Collections.singletonList(HTTPS_LOCALHOST_8443), HTTPS_LOCALHOST))
                .build();
    }

    public static Stream<Arguments> invalidAliasProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(HTTPS_LOWKEY_VAULT_8443, Set.of(HTTPS_LOCALHOST), HTTPS_LOCALHOST, null,
                        AlreadyExistsException.class))
                .add(Arguments.of(HTTPS_LOWKEY_VAULT_8443, Set.of(HTTPS_LOCALHOST), HTTPS_LOWKEY_VAULT_8443, null,
                        AlreadyExistsException.class))
                .add(Arguments.of(HTTPS_LOWKEY_VAULT_8443, Set.of(HTTPS_LOCALHOST), null, null,
                        IllegalArgumentException.class))
                .add(Arguments.of(HTTPS_LOWKEY_VAULT_8443, Set.of(HTTPS_LOCALHOST), HTTPS_LOCALHOST_80, HTTPS_LOCALHOST_80,
                        IllegalArgumentException.class))
                .build();
    }

    public static Stream<Arguments> validAliasProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(HTTPS_LOWKEY_VAULT_8443, Set.of(HTTPS_LOCALHOST), null, HTTPS_LOCALHOST,
                        Set.of()))
                .add(Arguments.of(HTTPS_LOWKEY_VAULT_8443, Set.of(HTTPS_LOCALHOST), HTTPS_LOCALHOST_80, HTTPS_LOCALHOST,
                        Set.of(HTTPS_LOCALHOST_80)))
                .add(Arguments.of(HTTPS_LOWKEY_VAULT_8443, Set.of(HTTPS_LOCALHOST), HTTPS_LOCALHOST_80, null,
                        new TreeSet<>(Set.of(HTTPS_LOCALHOST, HTTPS_LOCALHOST_80))))
                .build();
    }

    public static Stream<Arguments> aliasValueProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(Collections.singletonList(HTTPS_LOCALHOST_8443), HTTPS_LOCALHOST,
                        Set.of(HTTPS_LOCALHOST_8443)))
                .add(Arguments.of(List.of(HTTPS_LOCALHOST, HTTPS_LOCALHOST_80, HTTPS_LOCALHOST_8443), HTTPS_LOWKEY_VAULT_8443,
                        Set.of(HTTPS_DEFAULT_LOWKEY_VAULT, HTTPS_LOCALHOST_8443)))
                .build();
    }

    @ParameterizedTest
    @MethodSource("valueProvider")
    void testCreateShouldThrowExceptionWhenAlreadyExists(
            final List<URI> vaults,
            final URI duplicate) {
        //given
        final var underTest = new VaultServiceImpl(uri -> uri);
        vaults.forEach(underTest::create);

        //when
        Assertions.assertThrows(AlreadyExistsException.class, () -> underTest.create(duplicate));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("aliasValueProvider")
    void testCreateShouldThrowExceptionWhenAlreadyExists(
            final List<URI> vaults,
            final URI baseUri,
            final Set<URI> duplicate) {
        //given
        final var underTest = new VaultServiceImpl(uri -> uri);
        vaults.forEach(underTest::create);

        //when
        Assertions.assertThrows(AlreadyExistsException.class,
                () -> underTest.create(baseUri, RecoveryLevel.PURGEABLE, 0, duplicate));

        //then + exception
    }

    @Test
    void testCreateShouldThrowExceptionWhenBaseUriMatchesAlias() {
        //given
        final var underTest = new VaultServiceImpl(uri -> uri);
        final var httpsLocalhost = Set.of(HTTPS_LOCALHOST);

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> underTest.create(HTTPS_LOCALHOST, RecoveryLevel.PURGEABLE, 0, httpsLocalhost));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("valueProvider")
    void testDeleteShouldReturnFalseWhenAlreadyDeleted(
            final List<URI> vaults,
            final URI delete) {
        //given
        final var underTest = new VaultServiceImpl(uri -> uri);
        vaults.forEach(underTest::create);

        //when
        final var actualOriginal = underTest.delete(delete);
        final var actualSecondTry = underTest.delete(delete);

        //then
        Assertions.assertTrue(actualOriginal);
        Assertions.assertFalse(actualSecondTry);
    }

    @ParameterizedTest
    @MethodSource("valueProvider")
    void testRecoverShouldThrowExceptionWhenNotDeleted(
            final List<URI> vaults,
            final URI duplicate) {
        //given
        final var underTest = new VaultServiceImpl(uri -> uri);
        vaults.forEach(underTest::create);

        //when
        Assertions.assertThrows(NotFoundException.class, () -> underTest.recover(duplicate));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("valueProvider")
    void testPurgeShouldThrowExceptionWhenNotDeleted(
            final List<URI> vaults,
            final URI duplicate) {
        //given
        final var underTest = new VaultServiceImpl(uri -> uri);
        vaults.forEach(underTest::create);

        //when
        Assertions.assertThrows(NotFoundException.class, () -> underTest.purge(duplicate));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("valueProvider")
    void testFindByUriIncludeDeletedShouldReturnValueWhenItMatchesFully(
            final List<URI> vaults,
            final URI lookup) {
        //given
        final var underTest = new VaultServiceImpl(uri -> uri);
        vaults.forEach(underTest::create);
        vaults.stream().limit(2).forEach(underTest::delete);

        //when
        final var actual = underTest.findByUriIncludeDeleted(lookup);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(lookup, actual.baseUri());
    }

    @ParameterizedTest
    @MethodSource("missingValueProvider")
    void testFindByUriIncludeDeletedShouldThrowExceptionWhenItemDoesNotMatchFully(
            final List<URI> vaults,
            final URI lookup) {
        //given
        final var underTest = new VaultServiceImpl(uri -> uri);
        vaults.forEach(underTest::create);
        vaults.stream().limit(2).forEach(underTest::delete);

        //when
        Assertions.assertThrows(NotFoundException.class, () -> underTest.findByUriIncludeDeleted(lookup));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("valueProvider")
    void testFindByUriShouldReturnValueWhenItMatchesFully(
            final List<URI> vaults,
            final URI lookup) {
        //given
        final var underTest = new VaultServiceImpl(uri -> uri);
        vaults.forEach(underTest::create);

        //when
        final var actual = underTest.findByUri(lookup);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(lookup, actual.baseUri());
    }

    @ParameterizedTest
    @MethodSource("valueProvider")
    void testFindByUriShouldReturnValueWhenItMatchesFullyAfterRecovery(
            final List<URI> vaults,
            final URI lookup) {
        //given
        final var underTest = new VaultServiceImpl(uri -> uri);
        vaults.forEach(uri -> underTest.create(uri, RecoveryLevel.RECOVERABLE, RecoveryLevel.MAX_RECOVERABLE_DAYS_INCLUSIVE, null));
        vaults.forEach(underTest::delete);
        vaults.forEach(underTest::recover);

        //when
        final var actual = underTest.findByUri(lookup);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(lookup, actual.baseUri());
    }

    @ParameterizedTest
    @MethodSource("valueProvider")
    void testFindByUriShouldNotReturnValueWhenItWasPurged(
            final List<URI> vaults,
            final URI lookup) {
        //given
        final var underTest = new VaultServiceImpl(uri -> uri);
        vaults.forEach(uri -> underTest.create(uri, RecoveryLevel.RECOVERABLE, RecoveryLevel.MAX_RECOVERABLE_DAYS_INCLUSIVE, null));
        vaults.forEach(underTest::delete);
        vaults.forEach(underTest::purge);

        //when
        Assertions.assertThrows(NotFoundException.class, () -> underTest.findByUri(lookup));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("missingValueProvider")
    void testFindByUriShouldThrowExceptionWhenItemDoesNotMatchFully(
            final List<URI> vaults,
            final URI lookup) {
        //given
        final var underTest = new VaultServiceImpl(uri -> uri);
        vaults.forEach(underTest::create);

        //when
        Assertions.assertThrows(NotFoundException.class, () -> underTest.findByUri(lookup));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("valueProvider")
    void testFindByUriShouldThrowExceptionWhenItemIsDeleted(
            final List<URI> vaults,
            final URI lookup) {
        //given
        final var underTest = new VaultServiceImpl(uri -> uri);
        vaults.forEach(uri -> underTest
                .create(uri, RecoveryLevel.CUSTOMIZED_RECOVERABLE, RecoveryLevel.MIN_RECOVERABLE_DAYS_INCLUSIVE, null));
        vaults.forEach(underTest::delete);


        //when
        Assertions.assertThrows(NotFoundException.class, () -> underTest.findByUri(lookup));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("valueMapProvider")
    void testListAndListDeletedShouldFilterBasedOnDeletedStatusWhenCalled(final Map<URI, Boolean> vaults) {
        //given
        final var underTest = new VaultServiceImpl(uri -> uri);
        vaults.forEach((k, v) -> {
            underTest.create(k, RecoveryLevel.CUSTOMIZED_RECOVERABLE, RecoveryLevel.MIN_RECOVERABLE_DAYS_INCLUSIVE, null);
            if (v) {
                underTest.delete(k);
            }
        });

        //when
        final var actualActive = underTest.list();
        final var actualDeleted = underTest.listDeleted();

        //then
        vaults.forEach((k, v) -> {
            if (v) {
                Assertions.assertTrue(actualActive.stream().noneMatch(vault -> vault.matches(k, uri -> uri)));
                Assertions.assertTrue(actualDeleted.stream().anyMatch(vault -> vault.matches(k, uri -> uri)));
            } else {
                Assertions.assertTrue(actualDeleted.stream().noneMatch(vault -> vault.matches(k, uri -> uri)));
                Assertions.assertTrue(actualActive.stream().anyMatch(vault -> vault.matches(k, uri -> uri)));
            }
        });
    }

    @ParameterizedTest
    @MethodSource("valueMapProvider")
    @SuppressWarnings("java:S2925") //this is a fixed duration wait to let the timestamps change
    void testListDeletedShouldNotReturnPurgedItemsWhenCalled(final Map<URI, Boolean> vaults) {
        //given
        final var underTest = new VaultServiceImpl(uri -> uri);
        vaults.forEach((k, v) -> {
            if (v) {
                underTest.create(k, RecoveryLevel.PURGEABLE, null, null);
            } else {
                underTest.create(k, RecoveryLevel.CUSTOMIZED_RECOVERABLE, RecoveryLevel.MIN_RECOVERABLE_DAYS_INCLUSIVE, null);
            }
            underTest.delete(k);
        });
        Assertions.assertDoesNotThrow(() -> Thread.sleep(WAIT_MILLIS));

        //when
        final var actualDeleted = underTest.listDeleted();

        //then
        vaults.forEach((k, v) -> {
            if (v) {
                Assertions.assertTrue(actualDeleted.stream().noneMatch(vault -> vault.matches(k, uri -> uri)));
            } else {
                Assertions.assertTrue(actualDeleted.stream().anyMatch(vault -> vault.matches(k, uri -> uri)));
            }
        });
    }

    @ParameterizedTest
    @ValueSource(ints = {-42, -10, -5, -3, -2, -1, 0})
    void testTimeShiftShouldThrowExceptionWhenCalledWithNegativeOrZero(final int value) {
        //given
        final var underTest = new VaultServiceImpl(uri -> uri);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.timeShift(value, false));

        //then + exception
    }

    @Test
    void testTimeShiftShouldBeForwardedToEachVaultWhenCalledWithPositive() {
        //given
        final var underTest = new VaultServiceImpl(uri -> uri);
        final var vaultFake = underTest.create(HTTPS_LOWKEY_VAULT_8443);
        final var createdOriginal = vaultFake.getCreatedOn();

        //when
        underTest.timeShift(TestConstants.NUMBER_OF_SECONDS_IN_10_MINUTES, false);

        //then
        Assertions.assertEquals(createdOriginal.minusSeconds(TestConstants.NUMBER_OF_SECONDS_IN_10_MINUTES), vaultFake.getCreatedOn());
    }

    @ParameterizedTest
    @MethodSource("invalidAliasProvider")
    void testUpdateAliasShouldThrowExceptionWhenCalledWithInvalidInput(
            final URI baseUri,
            final Set<URI> aliases,
            final URI add,
            final URI remove,
            final Class<Exception> expectedException) {
        //given
        final var underTest = new VaultServiceImpl(uri -> uri);
        underTest.create(baseUri, RecoveryLevel.CUSTOMIZED_RECOVERABLE, RecoveryLevel.MAX_RECOVERABLE_DAYS_INCLUSIVE, aliases);

        //when
        Assertions.assertThrows(expectedException, () -> underTest.updateAlias(baseUri, add, remove));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("validAliasProvider")
    void testUpdateAliasShouldAddAndRemoveAliasesWhenCalledWithValidInput(
            final URI baseUri,
            final Set<URI> aliases,
            final URI add,
            final URI remove,
            final Set<URI> expected) {
        //given
        final var underTest = new VaultServiceImpl(uri -> uri);
        underTest.create(baseUri, RecoveryLevel.CUSTOMIZED_RECOVERABLE, RecoveryLevel.MAX_RECOVERABLE_DAYS_INCLUSIVE, aliases);

        //when
        final var actual = underTest.updateAlias(baseUri, add, remove);

        //then
        Assertions.assertIterableEquals(expected, new TreeSet<>(actual.aliases()));
    }

    @Test
    void testUpdateAliasShouldThrowExceptionWhenVaultNotFound() {
        //given
        final var underTest = new VaultServiceImpl(uri -> uri);
        underTest.create(HTTPS_DEFAULT_LOWKEY_VAULT_8443);

        //when
        Assertions.assertThrows(NotFoundException.class, () -> underTest.updateAlias(HTTPS_LOCALHOST, HTTPS_LOCALHOST_80, null));

        //then + exception
    }
}
