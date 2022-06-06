package com.github.nagyesta.lowkeyvault.service.vault.impl;

import com.github.nagyesta.lowkeyvault.TestConstants;
import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.service.exception.AlreadyExistsException;
import com.github.nagyesta.lowkeyvault.service.exception.NotFoundException;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

    @ParameterizedTest
    @MethodSource("valueProvider")
    void testCreateShouldThrowExceptionWhenAlreadyExists(final List<URI> vaults, final URI duplicate) {
        //given
        final VaultServiceImpl underTest = new VaultServiceImpl();
        vaults.forEach(underTest::create);

        //when
        Assertions.assertThrows(AlreadyExistsException.class, () -> underTest.create(duplicate));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("valueProvider")
    void testDeleteShouldReturnFalseWhenAlreadyDeleted(final List<URI> vaults, final URI delete) {
        //given
        final VaultServiceImpl underTest = new VaultServiceImpl();
        vaults.forEach(underTest::create);

        //when
        final boolean actualOriginal = underTest.delete(delete);
        final boolean actualSecondTry = underTest.delete(delete);

        //then
        Assertions.assertTrue(actualOriginal);
        Assertions.assertFalse(actualSecondTry);
    }

    @ParameterizedTest
    @MethodSource("valueProvider")
    void testRecoverShouldThrowExceptionWhenNotDeleted(final List<URI> vaults, final URI duplicate) {
        //given
        final VaultServiceImpl underTest = new VaultServiceImpl();
        vaults.forEach(underTest::create);

        //when
        Assertions.assertThrows(NotFoundException.class, () -> underTest.recover(duplicate));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("valueProvider")
    void testPurgeShouldThrowExceptionWhenNotDeleted(final List<URI> vaults, final URI duplicate) {
        //given
        final VaultServiceImpl underTest = new VaultServiceImpl();
        vaults.forEach(underTest::create);

        //when
        Assertions.assertThrows(NotFoundException.class, () -> underTest.purge(duplicate));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("valueProvider")
    void testFindByUriIncludeDeletedShouldReturnValueWhenItMatchesFully(final List<URI> vaults, final URI lookup) {
        //given
        final VaultServiceImpl underTest = new VaultServiceImpl();
        vaults.forEach(underTest::create);
        vaults.stream().limit(2).forEach(underTest::delete);

        //when
        final VaultFake actual = underTest.findByUriIncludeDeleted(lookup);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(lookup, actual.baseUri());
    }

    @ParameterizedTest
    @MethodSource("missingValueProvider")
    void testFindByUriIncludeDeletedShouldThrowExceptionWhenItemDoesNotMatchFully(final List<URI> vaults, final URI lookup) {
        //given
        final VaultServiceImpl underTest = new VaultServiceImpl();
        vaults.forEach(underTest::create);
        vaults.stream().limit(2).forEach(underTest::delete);

        //when
        Assertions.assertThrows(NotFoundException.class, () -> underTest.findByUriIncludeDeleted(lookup));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("valueProvider")
    void testFindByUriShouldReturnValueWhenItMatchesFully(final List<URI> vaults, final URI lookup) {
        //given
        final VaultServiceImpl underTest = new VaultServiceImpl();
        vaults.forEach(underTest::create);

        //when
        final VaultFake actual = underTest.findByUri(lookup);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(lookup, actual.baseUri());
    }

    @ParameterizedTest
    @MethodSource("valueProvider")
    void testFindByUriShouldReturnValueWhenItMatchesFullyAfterRecovery(final List<URI> vaults, final URI lookup) {
        //given
        final VaultServiceImpl underTest = new VaultServiceImpl();
        vaults.forEach(uri -> underTest.create(uri, RecoveryLevel.RECOVERABLE, RecoveryLevel.MAX_RECOVERABLE_DAYS_INCLUSIVE));
        vaults.forEach(underTest::delete);
        vaults.forEach(underTest::recover);

        //when
        final VaultFake actual = underTest.findByUri(lookup);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(lookup, actual.baseUri());
    }

    @ParameterizedTest
    @MethodSource("valueProvider")
    void testFindByUriShouldNotReturnValueWhenItWasPurged(final List<URI> vaults, final URI lookup) {
        //given
        final VaultServiceImpl underTest = new VaultServiceImpl();
        vaults.forEach(uri -> underTest.create(uri, RecoveryLevel.RECOVERABLE, RecoveryLevel.MAX_RECOVERABLE_DAYS_INCLUSIVE));
        vaults.forEach(underTest::delete);
        vaults.forEach(underTest::purge);

        //when
        Assertions.assertThrows(NotFoundException.class, () -> underTest.findByUri(lookup));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("missingValueProvider")
    void testFindByUriShouldThrowExceptionWhenItemDoesNotMatchFully(final List<URI> vaults, final URI lookup) {
        //given
        final VaultServiceImpl underTest = new VaultServiceImpl();
        vaults.forEach(underTest::create);

        //when
        Assertions.assertThrows(NotFoundException.class, () -> underTest.findByUri(lookup));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("valueProvider")
    void testFindByUriShouldThrowExceptionWhenItemIsDeleted(final List<URI> vaults, final URI lookup) {
        //given
        final VaultServiceImpl underTest = new VaultServiceImpl();
        vaults.forEach(uri -> underTest.create(uri, RecoveryLevel.CUSTOMIZED_RECOVERABLE, RecoveryLevel.MIN_RECOVERABLE_DAYS_INCLUSIVE));
        vaults.forEach(underTest::delete);


        //when
        Assertions.assertThrows(NotFoundException.class, () -> underTest.findByUri(lookup));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("valueMapProvider")
    void testListAndListDeletedShouldFilterBasedOnDeletedStatusWhenCalled(final Map<URI, Boolean> vaults) {
        //given
        final VaultServiceImpl underTest = new VaultServiceImpl();
        vaults.forEach((k, v) -> {
            underTest.create(k, RecoveryLevel.CUSTOMIZED_RECOVERABLE, RecoveryLevel.MIN_RECOVERABLE_DAYS_INCLUSIVE);
            if (v) {
                underTest.delete(k);
            }
        });

        //when
        final List<VaultFake> actualActive = underTest.list();
        final List<VaultFake> actualDeleted = underTest.listDeleted();

        //then
        vaults.forEach((k, v) -> {
            if (v) {
                Assertions.assertTrue(actualActive.stream().noneMatch(vault -> vault.matches(k)));
                Assertions.assertTrue(actualDeleted.stream().anyMatch(vault -> vault.matches(k)));
            } else {
                Assertions.assertTrue(actualDeleted.stream().noneMatch(vault -> vault.matches(k)));
                Assertions.assertTrue(actualActive.stream().anyMatch(vault -> vault.matches(k)));
            }
        });
    }

    @ParameterizedTest
    @MethodSource("valueMapProvider")
    void testListDeletedShouldNotReturnPurgedItemsWhenCalled(final Map<URI, Boolean> vaults) {
        //given
        final VaultServiceImpl underTest = new VaultServiceImpl();
        vaults.forEach((k, v) -> {
            if (v) {
                underTest.create(k, RecoveryLevel.PURGEABLE, null);
            } else {
                underTest.create(k, RecoveryLevel.CUSTOMIZED_RECOVERABLE, RecoveryLevel.MIN_RECOVERABLE_DAYS_INCLUSIVE);
            }
            underTest.delete(k);
        });
        Assertions.assertDoesNotThrow(() -> Thread.sleep(WAIT_MILLIS));

        //when
        final List<VaultFake> actualDeleted = underTest.listDeleted();

        //then
        vaults.forEach((k, v) -> {
            if (v) {
                Assertions.assertTrue(actualDeleted.stream().noneMatch(vault -> vault.matches(k)));
            } else {
                Assertions.assertTrue(actualDeleted.stream().anyMatch(vault -> vault.matches(k)));
            }
        });
    }

    @ParameterizedTest
    @ValueSource(ints = {-42, -10, -5, -3, -2, -1, 0})
    void testTimeShiftShouldThrowExceptionWhenCalledWithNegativeOrZero(final int value) {
        //given
        final VaultServiceImpl underTest = new VaultServiceImpl();

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.timeShift(value));

        //then + exception
    }

    @Test
    void testTimeShiftShouldBeForwardedToEachVaultWhenCalledWithPositive() {
        //given
        final VaultServiceImpl underTest = new VaultServiceImpl();
        final VaultFake vaultFake = underTest.create(HTTPS_LOWKEY_VAULT_8443);
        final OffsetDateTime createdOriginal = vaultFake.getCreatedOn();

        //when
        underTest.timeShift(TestConstants.NUMBER_OF_SECONDS_IN_10_MINUTES);

        //then
        Assertions.assertEquals(createdOriginal.minusSeconds(TestConstants.NUMBER_OF_SECONDS_IN_10_MINUTES), vaultFake.getCreatedOn());
    }
}
