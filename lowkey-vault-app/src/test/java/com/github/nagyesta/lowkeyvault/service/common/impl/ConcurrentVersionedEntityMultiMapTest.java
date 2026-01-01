package com.github.nagyesta.lowkeyvault.service.common.impl;

import com.github.nagyesta.lowkeyvault.TestConstants;
import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.service.exception.NotFoundException;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.id.KeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.impl.KeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.impl.RsaKeyVaultKeyEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstantsKeys.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOCALHOST_8443;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConcurrentVersionedEntityMultiMapTest {

    private
    ConcurrentVersionedEntityMultiMap<KeyEntityId, VersionedKeyEntityId, ReadOnlyKeyVaultKeyEntity, KeyVaultKeyEntity<?, ?>> underTest;
    private
    ConcurrentVersionedEntityMultiMap<KeyEntityId, VersionedKeyEntityId, ReadOnlyKeyVaultKeyEntity, KeyVaultKeyEntity<?, ?>> deleted;
    @Mock
    private RsaKeyVaultKeyEntity key1Version1Mock;
    @Mock
    private RsaKeyVaultKeyEntity key1Version2Mock;
    @Mock
    private RsaKeyVaultKeyEntity key1Version3Mock;
    @Mock
    private RsaKeyVaultKeyEntity key2Version1Mock;
    @Mock
    private RsaKeyVaultKeyEntity key2Version2Mock;
    @Mock
    private RsaKeyVaultKeyEntity key3Version1Mock;
    @Mock
    private RsaKeyVaultKeyEntity key3Version2Mock;
    private AutoCloseable openMocks;

    public static Stream<Arguments> recoveryProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(RecoveryLevel.PURGEABLE, null))
                .add(Arguments.of(RecoveryLevel.RECOVERABLE_AND_PURGEABLE, RecoveryLevel.MAX_RECOVERABLE_DAYS_INCLUSIVE))
                .add(Arguments.of(RecoveryLevel.CUSTOMIZED_RECOVERABLE, RecoveryLevel.MIN_RECOVERABLE_DAYS_INCLUSIVE))
                .build();
    }

    @BeforeEach
    void setUp() {
        underTest = new ConcurrentVersionedEntityMultiMap<>(RecoveryLevel.RECOVERABLE, RecoveryLevel.MAX_RECOVERABLE_DAYS_INCLUSIVE,
                (i, v) -> new VersionedKeyEntityId(HTTPS_LOCALHOST_8443, i, v), false);
        openMocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @Test
    void testListLatestEntitiesShouldReturnOnlyTheLatestVersionPerEntityWhenMultipleVersionsWerePut() {
        //given
        putAllMocks();

        //when
        final var actual = underTest.listLatestEntities();

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(3, actual.size());
        Assertions.assertSame(key1Version3Mock, actual.get(0));
        Assertions.assertSame(key2Version2Mock, actual.get(1));
        Assertions.assertSame(key3Version2Mock, actual.get(2));
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @Test
    void testListLatestNonManagedEntitiesShouldReturnOnlyTheNotManagedEntitiesWhenMultipleVersionsWerePut() {
        //given
        putAllMocks();
        when(key1Version3Mock.isManaged()).thenReturn(true);

        //when
        final var actual = underTest.listLatestNonManagedEntities();

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(2, actual.size());
        Assertions.assertSame(key2Version2Mock, actual.get(0));
        Assertions.assertSame(key3Version2Mock, actual.get(1));
    }

    @Test
    void testListLatestEntitiesShouldReturnEmptyListWhenEmpty() {
        //given

        //when
        final var actual = underTest.listLatestEntities();

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertIterableEquals(List.of(), actual);
    }

    @Test
    void testGetVersionsShouldReturnAllRelevantVersionsWhenCalledWithExistingKey() {
        //given
        putAllMocks();

        //when
        final var actual = underTest.getVersions(VERSIONED_KEY_ENTITY_ID_1_VERSION_1);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertIterableEquals(List.of(KEY_VERSION_1, KEY_VERSION_2, KEY_VERSION_3), actual);
    }

    @Test
    void testGetVersionsShouldReturnOnlyRelevantVersionsWhenCalledWithExistingKey() {
        //given
        putAllMocks();

        //when
        final var actual = underTest.getVersions(VERSIONED_KEY_ENTITY_ID_2_VERSION_1);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertIterableEquals(List.of(KEY_VERSION_1, KEY_VERSION_2), actual);
    }

    @Test
    void testGetVersionsShouldThrowExceptionWhenCalledWithNotFoundEntity() {
        //given
        underTest.put(VERSIONED_KEY_ENTITY_ID_1_VERSION_1, key1Version1Mock);

        //when
        Assertions.assertThrows(NotFoundException.class, () -> underTest.getVersions(VERSIONED_KEY_ENTITY_ID_3_VERSION_1));

        //then + exception
    }

    @Test
    void testGetVersionsShouldThrowExceptionWhenCalledWhileEmpty() {
        //given

        //when
        Assertions.assertThrows(NotFoundException.class, () -> underTest.getVersions(VERSIONED_KEY_ENTITY_ID_1_VERSION_1));

        //then + exception
    }

    @Test
    void testContainsNameShouldReturnTrueWhenCalledWithExistingKey() {
        //given
        putAllMocks();

        //when
        final var actual = underTest.containsName(KEY_NAME_1);

        //then
        Assertions.assertTrue(actual);
    }

    @Test
    void testContainsNameShouldReturnFalseWhenCalledWithNonExistingKey() {
        //given
        underTest.put(VERSIONED_KEY_ENTITY_ID_1_VERSION_1, key1Version1Mock);

        //when
        final var actual = underTest.containsName(KEY_NAME_2);

        //then
        Assertions.assertFalse(actual);
    }

    @Test
    void testContainsEntityShouldReturnTrueWhenCalledWithExistingKey() {
        //given
        putAllMocks();

        //when
        final var actual = underTest.containsEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_1);

        //then
        Assertions.assertTrue(actual);
    }

    @Test
    void testContainsEntityShouldReturnFalseWhenCalledWithNonExistingKeyVersion() {
        //given
        putAllMocks();

        //when
        final var actual = underTest.containsEntity(VERSIONED_KEY_ENTITY_ID_3_VERSION_3);

        //then
        Assertions.assertFalse(actual);
    }

    @Test
    void testContainsEntityShouldReturnFalseWhenCalledWithNonExistingKeyName() {
        //given

        //when
        final var actual = underTest.containsEntity(VERSIONED_KEY_ENTITY_ID_3_VERSION_3);

        //then
        Assertions.assertFalse(actual);
    }


    @Test
    void testAssertContainsEntityShouldSucceedWhenCalledWithExistingKey() {
        //given
        putAllMocks();

        //when
        Assertions.assertDoesNotThrow(() -> underTest.assertContainsEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_1));

        //then + no exception
    }

    @Test
    void testAssertContainsEntityShouldThrowExceptionWhenCalledWithNonExistingKey() {
        //given
        putAllMocks();

        //when
        Assertions.assertThrows(NotFoundException.class, () -> underTest.assertContainsEntity(VERSIONED_KEY_ENTITY_ID_3_VERSION_3));

        //then + exception
    }

    @Test
    void testGetLatestVersionOfEntityShouldThrowExceptionWhenMissingEntity() {
        //given

        //when
        Assertions.assertThrows(NotFoundException.class, () -> underTest.getLatestVersionOfEntity(UNVERSIONED_KEY_ENTITY_ID_3));

        //then + exception
    }

    @Test
    void testGetLatestVersionOfEntityIdShouldReturnEntityWhenFound() {
        //given
        putAllMocks();

        //when
        final var actual = underTest.getLatestVersionOfEntity(UNVERSIONED_KEY_ENTITY_ID_3);

        //then
        Assertions.assertEquals(VERSIONED_KEY_ENTITY_ID_3_VERSION_2, actual);
    }

    @Test
    void testGetReadOnlyEntityShouldReturnEntityWhenCalledWithExistingKey() {
        //given
        putAllMocks();

        //when
        final var actual = underTest.getReadOnlyEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_3);

        //then
        Assertions.assertSame(key1Version3Mock, actual);
    }

    @Test
    void testGetReadOnlyEntityShouldThrowExceptionWhenCalledWithMissingKey() {
        //given
        putAllMocks();

        //when
        Assertions.assertThrows(NotFoundException.class, () -> underTest.getReadOnlyEntity(VERSIONED_KEY_ENTITY_ID_3_VERSION_3));

        //then + exception
    }

    @Test
    void testGetEntityWithTypeShouldReturnEntityWhenCalledWithValidInput() {
        //given
        putAllMocks();

        //when
        final var actual = underTest.getEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_3, ReadOnlyKeyVaultKeyEntity.class);

        //then
        Assertions.assertSame(key1Version3Mock, actual);
    }

    @ParameterizedTest
    @MethodSource("recoveryProvider")
    void testGetRecoveryLevelAndDaysShouldReturnTheRecoveryLevelWhichWasSetPreviouslyWhenCalled(
            final RecoveryLevel recoveryLevel,
            final Integer recoverableDays) {
        //given
        underTest = new ConcurrentVersionedEntityMultiMap<>(recoveryLevel, recoverableDays,
                (i, v) -> new VersionedKeyEntityId(HTTPS_LOCALHOST_8443, i, v), false);

        //when
        final var actualRecoveryLevel = underTest.getRecoveryLevel();
        final var actualRecoverableDays = underTest.getRecoverableDays();

        //then
        Assertions.assertEquals(recoveryLevel, actualRecoveryLevel);
        Assertions.assertEquals(recoverableDays, actualRecoverableDays.orElse(null));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testIsDeletedShouldReturnTheRecoveryLevelWhichWasSetPreviouslyWhenCalled(final boolean deleted) {
        //given
        underTest = new ConcurrentVersionedEntityMultiMap<>(RecoveryLevel.PURGEABLE, null,
                (i, v) -> new VersionedKeyEntityId(HTTPS_LOCALHOST_8443, i, v), deleted);

        //when
        final var actual = underTest.isDeleted();

        //then
        Assertions.assertEquals(deleted, actual);
    }

    @ParameterizedTest
    @MethodSource("recoveryProvider")
    void testMoveToShouldMoveItemsBasedOnRecoveryLevelWhenCalled(
            final RecoveryLevel recoveryLevel,
            final Integer recoverableDays) {
        //given
        underTest = new ConcurrentVersionedEntityMultiMap<>(recoveryLevel, recoverableDays,
                (i, v) -> new VersionedKeyEntityId(HTTPS_LOCALHOST_8443, i, v), false);
        deleted = new ConcurrentVersionedEntityMultiMap<>(recoveryLevel, recoverableDays,
                (i, v) -> new VersionedKeyEntityId(HTTPS_LOCALHOST_8443, i, v), true);
        putAllMocks();

        //when
        underTest.moveTo(UNVERSIONED_KEY_ENTITY_ID_1, deleted, e -> {
            e.setDeletedDate(TestConstants.NOW);
            return e;
        });

        //then
        Assertions.assertFalse(underTest.containsName(KEY_NAME_1));
        Assertions.assertTrue(underTest.containsName(KEY_NAME_2));
        Assertions.assertTrue(underTest.containsName(KEY_NAME_3));
        if (recoveryLevel.isRecoverable()) {
            Assertions.assertTrue(deleted.containsName(KEY_NAME_1));
            verify(key1Version1Mock).setDeletedDate(TestConstants.NOW);
            verify(key1Version2Mock).setDeletedDate(TestConstants.NOW);
            verify(key1Version3Mock).setDeletedDate(TestConstants.NOW);
            Assertions.assertIterableEquals(List.of(key1Version3Mock), deleted.listLatestEntities());
        } else {
            Assertions.assertFalse(deleted.containsName(KEY_NAME_1));
            Assertions.assertIterableEquals(List.of(), deleted.listLatestEntities());
        }
    }

    @ParameterizedTest
    @MethodSource("recoveryProvider")
    void testPurgeExpiredShouldRemoveItemsRegardlessRecoveryLevelWhenCalled(
            final RecoveryLevel recoveryLevel,
            final Integer recoverableDays) {
        //given
        underTest = new ConcurrentVersionedEntityMultiMap<>(recoveryLevel, recoverableDays,
                (i, v) -> new VersionedKeyEntityId(HTTPS_LOCALHOST_8443, i, v), true);
        putAllMocks();
        Stream.of(key1Version1Mock, key1Version2Mock, key1Version3Mock)
                .forEach(mock -> when(mock.isPurgeExpired()).thenReturn(true));
        Stream.of(key2Version1Mock, key2Version2Mock, key3Version1Mock, key3Version2Mock)
                .forEach(mock -> when(mock.isPurgeExpired()).thenReturn(false));

        //when
        underTest.purgeExpired();

        //then
        Assertions.assertFalse(underTest.containsEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_1));
        Assertions.assertFalse(underTest.containsEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_2));
        Assertions.assertFalse(underTest.containsEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_3));
        Assertions.assertTrue(underTest.containsEntity(VERSIONED_KEY_ENTITY_ID_2_VERSION_1));
        Assertions.assertTrue(underTest.containsEntity(VERSIONED_KEY_ENTITY_ID_2_VERSION_2));
        Assertions.assertTrue(underTest.containsEntity(VERSIONED_KEY_ENTITY_ID_3_VERSION_1));
        Assertions.assertTrue(underTest.containsEntity(VERSIONED_KEY_ENTITY_ID_3_VERSION_2));
    }

    @Test
    void testPurgeExpiredShouldThrowExceptionWhenCalledOnNotDeletedMap() {
        //given
        underTest = new ConcurrentVersionedEntityMultiMap<>(RecoveryLevel.RECOVERABLE, RecoveryLevel.MAX_RECOVERABLE_DAYS_INCLUSIVE,
                (i, v) -> new VersionedKeyEntityId(HTTPS_LOCALHOST_8443, i, v), false);
        putAllMocks();

        //when
        Assertions.assertThrows(IllegalStateException.class, () -> underTest.purgeExpired());

        //then + exception
    }

    @Test
    void testPurgeDeletedShouldRemoveMatchingItemsWhenTheyCanBePurged() {
        //given
        underTest = new ConcurrentVersionedEntityMultiMap<>(
                RecoveryLevel.RECOVERABLE_AND_PURGEABLE, RecoveryLevel.MAX_RECOVERABLE_DAYS_INCLUSIVE,
                (i, v) -> new VersionedKeyEntityId(HTTPS_LOCALHOST_8443, i, v), true);
        putAllMocks();
        Stream.of(
                        key1Version1Mock, key1Version2Mock, key1Version3Mock,
                        key2Version1Mock, key2Version2Mock,
                        key3Version1Mock, key3Version2Mock
                )
                .forEach(mock -> {
                    when(mock.isPurgeExpired()).thenReturn(false);
                    when(mock.canPurge()).thenReturn(true);
                });

        //when
        underTest.purgeDeleted(UNVERSIONED_KEY_ENTITY_ID_3);

        //then
        Assertions.assertTrue(underTest.containsEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_1));
        Assertions.assertTrue(underTest.containsEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_2));
        Assertions.assertTrue(underTest.containsEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_3));
        Assertions.assertTrue(underTest.containsEntity(VERSIONED_KEY_ENTITY_ID_2_VERSION_1));
        Assertions.assertTrue(underTest.containsEntity(VERSIONED_KEY_ENTITY_ID_2_VERSION_2));
        Assertions.assertFalse(underTest.containsEntity(VERSIONED_KEY_ENTITY_ID_3_VERSION_1));
        Assertions.assertFalse(underTest.containsEntity(VERSIONED_KEY_ENTITY_ID_3_VERSION_2));
    }

    @Test
    void testPurgeDeletedShouldThrowExceptionWhenTheyCanNotBePurged() {
        //given
        underTest = new ConcurrentVersionedEntityMultiMap<>(
                RecoveryLevel.RECOVERABLE_AND_PURGEABLE, RecoveryLevel.MAX_RECOVERABLE_DAYS_INCLUSIVE,
                (i, v) -> new VersionedKeyEntityId(HTTPS_LOCALHOST_8443, i, v), true);
        putAllMocks();
        Stream.of(
                        key1Version1Mock, key1Version2Mock, key1Version3Mock,
                        key2Version1Mock, key2Version2Mock,
                        key3Version1Mock, key3Version2Mock
                )
                .forEach(mock -> {
                    when(mock.isPurgeExpired()).thenReturn(false);
                    when(mock.canPurge()).thenReturn(false);
                });

        //when
        Assertions.assertThrows(IllegalStateException.class, () -> underTest.purgeDeleted(UNVERSIONED_KEY_ENTITY_ID_3));

        //then + exception
        Assertions.assertTrue(underTest.containsEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_1));
        Assertions.assertTrue(underTest.containsEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_2));
        Assertions.assertTrue(underTest.containsEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_3));
        Assertions.assertTrue(underTest.containsEntity(VERSIONED_KEY_ENTITY_ID_2_VERSION_1));
        Assertions.assertTrue(underTest.containsEntity(VERSIONED_KEY_ENTITY_ID_2_VERSION_2));
        Assertions.assertTrue(underTest.containsEntity(VERSIONED_KEY_ENTITY_ID_3_VERSION_1));
        Assertions.assertTrue(underTest.containsEntity(VERSIONED_KEY_ENTITY_ID_3_VERSION_2));
    }

    @Test
    void testPurgeDeletedShouldThrowExceptionWhenCalledOnNotDeletedMap() {
        //given
        underTest = new ConcurrentVersionedEntityMultiMap<>(RecoveryLevel.RECOVERABLE, RecoveryLevel.MAX_RECOVERABLE_DAYS_INCLUSIVE,
                (i, v) -> new VersionedKeyEntityId(HTTPS_LOCALHOST_8443, i, v), false);
        putAllMocks();

        //when
        Assertions.assertThrows(IllegalStateException.class, () -> underTest.purgeDeleted(UNVERSIONED_KEY_ENTITY_ID_3));

        //then + exception
    }

    private void putAllMocks() {
        underTest.put(VERSIONED_KEY_ENTITY_ID_1_VERSION_1, key1Version1Mock);
        underTest.put(VERSIONED_KEY_ENTITY_ID_2_VERSION_1, key2Version1Mock);
        underTest.put(VERSIONED_KEY_ENTITY_ID_3_VERSION_1, key3Version1Mock);
        underTest.put(VERSIONED_KEY_ENTITY_ID_1_VERSION_2, key1Version2Mock);
        underTest.put(VERSIONED_KEY_ENTITY_ID_2_VERSION_2, key2Version2Mock);
        underTest.put(VERSIONED_KEY_ENTITY_ID_3_VERSION_2, key3Version2Mock);
        underTest.put(VERSIONED_KEY_ENTITY_ID_1_VERSION_3, key1Version3Mock);
    }
}
