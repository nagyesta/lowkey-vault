package com.github.nagyesta.lowkeyvault.service.key.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyCurveName;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyOperation;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.service.exception.AlreadyExistsException;
import com.github.nagyesta.lowkeyvault.service.exception.NotFoundException;
import com.github.nagyesta.lowkeyvault.service.key.KeyVaultFake;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.id.KeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.impl.VaultFakeImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstants.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsKeys.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOCALHOST;
import static org.mockito.Mockito.mock;

class KeyVaultFakeImplTest {

    private static final int DAYS = 42;
    private static final int COUNT = 10;
    private static final EcKeyCreationInput EC_KEY_CREATION_INPUT = new EcKeyCreationInput(KeyType.EC, KeyCurveName.P_256);

    public static Stream<Arguments> genericKeyCreateInputProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null))
                .add(Arguments.of(KEY_NAME_1, null))
                .add(Arguments.of(KEY_NAME_1, new KeyCreationInput<Integer>(KeyType.RSA_HSM, null)))
                .add(Arguments.of(KEY_NAME_1, new KeyCreationInput<Integer>(KeyType.RSA, null)))
                .add(Arguments.of(KEY_NAME_1, new KeyCreationInput<Integer>(KeyType.OCT_HSM, null)))
                .add(Arguments.of(KEY_NAME_1, new KeyCreationInput<KeyCurveName>(KeyType.EC, null)))
                .add(Arguments.of(KEY_NAME_1, new KeyCreationInput<KeyCurveName>(KeyType.EC_HSM, null)))
                .build();
    }

    public static Stream<Arguments> invalidKeyOperationsProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null))
                .add(Arguments.of(null, List.of()))
                .build();
    }

    public static Stream<Arguments> keyOperationsProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(List.of()))
                .add(Arguments.of(List.of(KeyOperation.ENCRYPT)))
                .add(Arguments.of(List.of(KeyOperation.ENCRYPT, KeyOperation.DECRYPT)))
                .add(Arguments.of(Arrays.asList(KeyOperation.values())))
                .build();
    }

    public static Stream<Arguments> nullProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null, null))
                .add(Arguments.of(mock(VaultFake.class), null, null))
                .add(Arguments.of(null, RecoveryLevel.PURGEABLE, null))
                .add(Arguments.of(null, null, 0))
                .add(Arguments.of(mock(VaultFake.class), RecoveryLevel.RECOVERABLE, null))
                .build();
    }

    @ParameterizedTest
    @MethodSource("nullProvider")
    void testConstructorShouldThrowExceptionWhenCalledWithNull(
            final VaultFake vaultFake, final RecoveryLevel recoveryLevel, final Integer recoverableDays) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> new KeyVaultFakeImpl(vaultFake, recoveryLevel, recoverableDays));

        //then + exception
    }

    @Test
    void testGetKeyVersionsShouldReturnAllKeyVersionsInChronologicalOrderWhenFound() {
        //given
        final KeyVaultFake underTest = createUnderTest();
        final List<String> expected = insertMultipleVersionsOfSameKey(underTest, KEY_NAME_1).stream()
                .map(KeyEntityId::version)
                .collect(Collectors.toList());
        underTest.createKeyVersion(KEY_NAME_2, EC_KEY_CREATION_INPUT);
        underTest.createKeyVersion(KEY_NAME_3, EC_KEY_CREATION_INPUT);

        final KeyEntityId keyEntityId = new KeyEntityId(HTTPS_LOCALHOST, KEY_NAME_1, null);

        //when
        final Deque<String> actual = underTest.getEntities().getVersions(keyEntityId);

        //then
        Assertions.assertIterableEquals(expected, actual);
    }

    @Test
    void testGetKeyVersionsShouldThrowExceptionWhenKeyIdIsNull() {
        //given
        final KeyVaultFake underTest = createUnderTest();
        insertMultipleVersionsOfSameKey(underTest, KEY_NAME_1);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.getEntities().getVersions(null));

        //then + exception
    }

    @Test
    void testGetKeyVersionsShouldThrowExceptionWhenNotFound() {
        //given
        final KeyVaultFake underTest = createUnderTest();
        insertMultipleVersionsOfSameKey(underTest, KEY_NAME_1);

        final KeyEntityId keyEntityId = new KeyEntityId(HTTPS_LOCALHOST, KEY_NAME_2, null);

        //when
        Assertions.assertThrows(NotFoundException.class, () -> underTest.getEntities().getVersions(keyEntityId));

        //then + exception
    }

    @Test
    void testGetLatestVersionOfEntityShouldThrowExceptionWhenKeyIdIsNull() {
        //given
        final KeyVaultFake underTest = createUnderTest();

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.getEntities().getLatestVersionOfEntity(null));

        //then + exception
    }

    @Test
    void testGetLatestVersionOfKeyShouldReturnAllKeyVersionsInChronologicalOrderWhenFound() {
        //given
        final KeyVaultFake underTest = createUnderTest();
        final String expected = insertMultipleVersionsOfSameKey(underTest, KEY_NAME_1).stream()
                .map(KeyEntityId::version)
                .skip(COUNT - 1)
                .findFirst().orElse(null);
        underTest.createKeyVersion(KEY_NAME_2, EC_KEY_CREATION_INPUT);
        underTest.createKeyVersion(KEY_NAME_3, EC_KEY_CREATION_INPUT);

        final KeyEntityId keyEntityId = new KeyEntityId(HTTPS_LOCALHOST, KEY_NAME_1, null);

        //when
        final VersionedKeyEntityId actual = underTest.getEntities().getLatestVersionOfEntity(keyEntityId);

        //then
        Assertions.assertEquals(expected, actual.version());
    }

    @ParameterizedTest
    @MethodSource("genericKeyCreateInputProvider")
    void testCreateKeyVersionShouldThrowExceptionWhenCalledWithNull(final String name, final KeyCreationInput<?> input) {
        //given
        final KeyVaultFake underTest = createUnderTest();

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.createKeyVersion(name, input));

        //then + exception
    }

    @SuppressWarnings("ConstantConditions")
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = KEY_NAME_1)
    void testCreateKeyVersionShouldThrowExceptionWhenCalledWithNullRsa(final String keyName) {
        //given
        final KeyVaultFake underTest = createUnderTest();
        final RsaKeyCreationInput input = null;

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.createRsaKeyVersion(keyName, input));

        //then + exception
    }

    @SuppressWarnings("ConstantConditions")
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = KEY_NAME_1)
    void testCreateKeyVersionShouldThrowExceptionWhenCalledWithNullEc(final String keyName) {
        //given
        final KeyVaultFake underTest = createUnderTest();
        final EcKeyCreationInput input = null;

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.createEcKeyVersion(keyName, input));

        //then + exception
    }

    @SuppressWarnings("ConstantConditions")
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = KEY_NAME_1)
    void testCreateKeyVersionShouldThrowExceptionWhenCalledWithNullOct(final String keyName) {
        //given
        final KeyVaultFake underTest = createUnderTest();
        final OctKeyCreationInput input = null;

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.createOctKeyVersion(keyName, input));

        //then + exception
    }

    @Test
    void testCreateKeyVersionShouldReturnIdWhenCalledWithValidRsaParameter() {
        //given
        final KeyVaultFake underTest = createUnderTest();
        final RsaKeyCreationInput input = new RsaKeyCreationInput(KeyType.RSA_HSM, null, null);

        //when
        final VersionedKeyEntityId actual = underTest.createKeyVersion(KEY_NAME_1, input);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertNotNull(actual.version());
        Assertions.assertEquals(HTTPS_LOCALHOST, actual.vault());
        Assertions.assertEquals(KEY_NAME_1, actual.id());
    }

    @Test
    void testCreateKeyVersionShouldReturnIdWhenCalledWithValidEcParameter() {
        //given
        final KeyVaultFake underTest = createUnderTest();
        final EcKeyCreationInput input = new EcKeyCreationInput(KeyType.EC_HSM, KeyCurveName.P_256);

        //when
        final VersionedKeyEntityId actual = underTest.createKeyVersion(KEY_NAME_1, input);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertNotNull(actual.version());
        Assertions.assertEquals(HTTPS_LOCALHOST, actual.vault());
        Assertions.assertEquals(KEY_NAME_1, actual.id());
    }

    @Test
    void testCreateKeyVersionShouldReturnIdWhenCalledWithValidOctParameter() {
        //given
        final KeyVaultFake underTest = createUnderTest();
        final OctKeyCreationInput input = new OctKeyCreationInput(KeyType.OCT_HSM, null);

        //when
        final VersionedKeyEntityId actual = underTest.createKeyVersion(KEY_NAME_1, input);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertNotNull(actual.version());
        Assertions.assertEquals(HTTPS_LOCALHOST, actual.vault());
        Assertions.assertEquals(KEY_NAME_1, actual.id());
    }

    @ParameterizedTest
    @MethodSource("keyOperationsProvider")
    void testSetKeyOperationsShouldUpdateListWhenCalledWithValidValues(final List<KeyOperation> list) {
        //given
        final KeyVaultFake underTest = createUnderTest();
        final OctKeyCreationInput input = new OctKeyCreationInput(KeyType.OCT_HSM, null);
        final VersionedKeyEntityId keyEntityId = underTest.createKeyVersion(KEY_NAME_1, input);

        //when
        underTest.setKeyOperations(keyEntityId, list);
        final ReadOnlyKeyVaultKeyEntity actual = underTest.getEntities().getReadOnlyEntity(keyEntityId);

        //then
        Assertions.assertIterableEquals(list, actual.getOperations());
    }

    @ParameterizedTest
    @MethodSource("invalidKeyOperationsProvider")
    void testSetKeyOperationsShouldThrowExceptionWhenCalledWithInvalidValues(
            final VersionedKeyEntityId keyEntityId, final List<KeyOperation> list) {
        //given
        final KeyVaultFake underTest = createUnderTest();

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.setKeyOperations(keyEntityId, list));

        //then + exception
    }

    @Test
    void testClearTagsShouldClearPreviouslySetTagsWhenCalledOnValidKey() {
        //given
        final KeyVaultFake underTest = createUnderTest();
        final OctKeyCreationInput input = new OctKeyCreationInput(KeyType.OCT_HSM, null);
        final VersionedKeyEntityId keyEntityId = underTest.createKeyVersion(KEY_NAME_1, input);

        //when
        underTest.addTags(keyEntityId, TAGS_TWO_KEYS);
        final ReadOnlyKeyVaultKeyEntity check = underTest.getEntities().getReadOnlyEntity(keyEntityId);
        Assumptions.assumeTrue(check.getTags().containsKey(KEY_1));
        Assumptions.assumeTrue(check.getTags().containsKey(KEY_2));
        underTest.clearTags(keyEntityId);

        final ReadOnlyKeyVaultKeyEntity actual = underTest.getEntities().getReadOnlyEntity(keyEntityId);

        //then
        Assertions.assertEquals(Collections.emptyMap(), actual.getTags());
    }

    @Test
    void testClearTagsShouldThrowExceptionWhenCalledWithNullKey() {
        //given
        final KeyVaultFake underTest = createUnderTest();

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.clearTags(null));

        //then + exception
    }

    @Test
    void testAddTagsShouldThrowExceptionWhenCalledWithNullKey() {
        //given
        final KeyVaultFake underTest = createUnderTest();

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.addTags(null, TAGS_EMPTY));

        //then + exception
    }

    @Test
    void testSetEnabledShouldReplacePreviouslySetValueWhenCalledOnValidKey() {
        //given
        final KeyVaultFake underTest = createUnderTest();
        final OctKeyCreationInput input = new OctKeyCreationInput(KeyType.OCT_HSM, null);
        final VersionedKeyEntityId keyEntityId = underTest.createKeyVersion(KEY_NAME_1, input);
        final ReadOnlyKeyVaultKeyEntity check = underTest.getEntities().getReadOnlyEntity(keyEntityId);
        Assertions.assertTrue(check.isEnabled());

        //when
        underTest.setEnabled(keyEntityId, false);
        final ReadOnlyKeyVaultKeyEntity actual = underTest.getEntities().getReadOnlyEntity(keyEntityId);

        //then
        Assertions.assertFalse(actual.isEnabled());
    }

    @Test
    void testSetEnabledShouldThrowExceptionWhenCalledWithNullKey() {
        //given
        final KeyVaultFake underTest = createUnderTest();

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.setEnabled(null, true));

        //then + exception
    }

    @Test
    void testSetExpiryShouldReplacePreviouslySetValueWhenCalledOnValidKey() {
        //given
        final KeyVaultFake underTest = createUnderTest();
        final OctKeyCreationInput input = new OctKeyCreationInput(KeyType.OCT_HSM, null);
        final VersionedKeyEntityId keyEntityId = underTest.createKeyVersion(KEY_NAME_1, input);
        final ReadOnlyKeyVaultKeyEntity check = underTest.getEntities().getReadOnlyEntity(keyEntityId);
        Assertions.assertTrue(check.getExpiry().isEmpty());
        Assertions.assertTrue(check.getNotBefore().isEmpty());

        //when
        underTest.setExpiry(keyEntityId, TIME_10_MINUTES_AGO, TIME_IN_10_MINUTES);
        final ReadOnlyKeyVaultKeyEntity actual = underTest.getEntities().getReadOnlyEntity(keyEntityId);

        //then
        Assertions.assertTrue(actual.getNotBefore().isPresent());
        Assertions.assertTrue(actual.getExpiry().isPresent());
        Assertions.assertEquals(TIME_10_MINUTES_AGO, actual.getNotBefore().get());
        Assertions.assertEquals(TIME_IN_10_MINUTES, actual.getExpiry().get());
    }

    @Test
    void testSetExpiryShouldReplacePreviouslySetValueWhenCalledOnValidKeyAndNotBeforeOnly() {
        //given
        final KeyVaultFake underTest = createUnderTest();
        final OctKeyCreationInput input = new OctKeyCreationInput(KeyType.OCT_HSM, null);
        final VersionedKeyEntityId keyEntityId = underTest.createKeyVersion(KEY_NAME_1, input);
        final ReadOnlyKeyVaultKeyEntity check = underTest.getEntities().getReadOnlyEntity(keyEntityId);
        Assertions.assertTrue(check.getExpiry().isEmpty());
        Assertions.assertTrue(check.getNotBefore().isEmpty());

        //when
        underTest.setExpiry(keyEntityId, TIME_10_MINUTES_AGO, null);
        final ReadOnlyKeyVaultKeyEntity actual = underTest.getEntities().getReadOnlyEntity(keyEntityId);

        //then
        Assertions.assertTrue(actual.getNotBefore().isPresent());
        Assertions.assertTrue(actual.getExpiry().isEmpty());
        Assertions.assertEquals(TIME_10_MINUTES_AGO, actual.getNotBefore().get());
    }

    @Test
    void testSetExpiryShouldReplacePreviouslySetValueWhenCalledOnValidKeyAndExpiryOnly() {
        //given
        final KeyVaultFake underTest = createUnderTest();
        final OctKeyCreationInput input = new OctKeyCreationInput(KeyType.OCT_HSM, null);
        final VersionedKeyEntityId keyEntityId = underTest.createKeyVersion(KEY_NAME_1, input);
        final ReadOnlyKeyVaultKeyEntity check = underTest.getEntities().getReadOnlyEntity(keyEntityId);
        Assertions.assertTrue(check.getExpiry().isEmpty());
        Assertions.assertTrue(check.getNotBefore().isEmpty());

        //when
        underTest.setExpiry(keyEntityId, null, TIME_IN_10_MINUTES);
        final ReadOnlyKeyVaultKeyEntity actual = underTest.getEntities().getReadOnlyEntity(keyEntityId);

        //then
        Assertions.assertTrue(actual.getNotBefore().isEmpty());
        Assertions.assertTrue(actual.getExpiry().isPresent());
        Assertions.assertEquals(TIME_IN_10_MINUTES, actual.getExpiry().get());
    }

    @Test
    void testSetExpiryShouldThrowExceptionWhenCalledWithNullKey() {
        //given
        final KeyVaultFake underTest = createUnderTest();

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> underTest.setExpiry(null, TIME_10_MINUTES_AGO, TIME_IN_10_MINUTES));

        //then + exception
    }

    @Test
    void testSetExpiryShouldThrowExceptionWhenCalledWithNegativeTimeDuration() {
        //given
        final KeyVaultFake underTest = createUnderTest();
        final OctKeyCreationInput input = new OctKeyCreationInput(KeyType.OCT_HSM, null);
        final VersionedKeyEntityId keyEntityId = underTest.createOctKeyVersion(KEY_NAME_1, input);

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> underTest.setExpiry(keyEntityId, TIME_IN_10_MINUTES, TIME_10_MINUTES_AGO));

        //then + exception
    }

    @Test
    void testConstructorWithRecoveryShouldThrowExceptionWhenCalledWithInvalidData() {
        //given
        final VaultFakeImpl vaultFake = new VaultFakeImpl(HTTPS_LOCALHOST);

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new KeyVaultFakeImpl(vaultFake, RecoveryLevel.PURGEABLE, DAYS));

        //then + exception
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testConstructorWithRecoveryShouldThrowExceptionWhenCalledWithNullRecoveryLevel() {
        //given
        final VaultFakeImpl vaultFake = new VaultFakeImpl(HTTPS_LOCALHOST);

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new KeyVaultFakeImpl(vaultFake, null, DAYS));

        //then + exception
    }

    @Test
    void testGetEntityShouldReturnValueWhenCalledWithExistingKey() {
        //given
        final KeyVaultFake underTest = createUnderTest();
        final OctKeyCreationInput input = new OctKeyCreationInput(KeyType.OCT_HSM, null);
        final VersionedKeyEntityId keyEntityId = underTest.createKeyVersion(KEY_NAME_1, input);

        //when
        final ReadOnlyKeyVaultKeyEntity actual = underTest.getEntities().getReadOnlyEntity(keyEntityId);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(keyEntityId.asUri(), actual.getUri());
    }

    @Test
    void testRawGetEntityShouldThrowExceptionWhenCalledWithNullKey() {
        //given
        final KeyVaultFake underTest = createUnderTest();

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.getEntities().getReadOnlyEntity(null));

        //then + exception
    }

    @Test
    void testCreateShouldThrowExceptionWhenCalledWithAlreadyDeletedKey() {
        //given
        final KeyVaultFake underTest = createUnderTest();
        insertMultipleVersionsOfSameKey(underTest, KEY_NAME_1);
        underTest.delete(UNVERSIONED_KEY_ENTITY_ID_1);

        //when
        Assertions.assertThrows(AlreadyExistsException.class, () -> underTest.createEcKeyVersion(KEY_NAME_1, EC_KEY_CREATION_INPUT));

        //then + exception
    }

    @Test
    void testDeleteShouldThrowExceptionWhenCalledWithMissingKey() {
        //given
        final KeyVaultFake underTest = createUnderTest();
        insertMultipleVersionsOfSameKey(underTest, KEY_NAME_1);

        //when
        Assertions.assertThrows(NotFoundException.class, () -> underTest.delete(UNVERSIONED_KEY_ENTITY_ID_2));

        //then + exception
    }

    @Test
    void testDeleteShouldMoveEntityToDeletedWhenCalledWithExistingKey() {
        //given
        final KeyVaultFake underTest = createUnderTest();
        insertMultipleVersionsOfSameKey(underTest, KEY_NAME_1);
        insertMultipleVersionsOfSameKey(underTest, KEY_NAME_2);
        insertMultipleVersionsOfSameKey(underTest, KEY_NAME_3);
        Assertions.assertFalse(underTest.getDeletedEntities().containsName(KEY_NAME_1));

        //when
        underTest.delete(UNVERSIONED_KEY_ENTITY_ID_1);

        //then
        Assertions.assertTrue(underTest.getDeletedEntities().containsName(KEY_NAME_1));
        Assertions.assertEquals(COUNT, underTest.getDeletedEntities().getVersions(UNVERSIONED_KEY_ENTITY_ID_1).size());
    }

    @Test
    void testDeleteShouldThrowExceptionWhenCalledWithNullKey() {
        //given
        final KeyVaultFake underTest = createUnderTest();

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.delete(null));

        //then + exception
    }

    @Test
    void testRecoverShouldThrowExceptionWhenCalledWithMissingDeletedKey() {
        //given
        final KeyVaultFake underTest = createUnderTest();
        insertMultipleVersionsOfSameKey(underTest, KEY_NAME_1);
        Assertions.assertFalse(underTest.getDeletedEntities().containsName(KEY_NAME_1));

        //when
        Assertions.assertThrows(NotFoundException.class, () -> underTest.recover(UNVERSIONED_KEY_ENTITY_ID_1));

        //then + exception
    }

    @Test
    void testRecoverShouldMoveEntityFromDeletedWhenCalledWithExistingDeletedKey() {
        //given
        final KeyVaultFake underTest = createUnderTest();
        insertMultipleVersionsOfSameKey(underTest, KEY_NAME_1);
        insertMultipleVersionsOfSameKey(underTest, KEY_NAME_2);
        insertMultipleVersionsOfSameKey(underTest, KEY_NAME_3);
        underTest.delete(UNVERSIONED_KEY_ENTITY_ID_1);
        Assertions.assertTrue(underTest.getDeletedEntities().containsName(KEY_NAME_1));

        //when
        underTest.recover(UNVERSIONED_KEY_ENTITY_ID_1);

        //then
        Assertions.assertFalse(underTest.getDeletedEntities().containsName(KEY_NAME_1));
        Assertions.assertEquals(COUNT, underTest.getEntities().getVersions(UNVERSIONED_KEY_ENTITY_ID_1).size());
    }

    @Test
    void testRecoverShouldThrowExceptionWhenCalledWithNullKey() {
        //given
        final KeyVaultFake underTest = createUnderTest();

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.recover(null));

        //then + exception
    }



    @Test
    void testPurgeShouldThrowExceptionWhenCalledWithMissingDeletedKey() {
        //given
        final KeyVaultFake underTest = createUnderTest();
        insertMultipleVersionsOfSameKey(underTest, KEY_NAME_1);
        Assertions.assertFalse(underTest.getDeletedEntities().containsName(KEY_NAME_1));

        //when
        Assertions.assertThrows(NotFoundException.class, () -> underTest.purge(UNVERSIONED_KEY_ENTITY_ID_1));

        //then + exception
    }

    @Test
    void testPurgeShouldRemoveEntityFromDeletedWhenCalledWithExistingDeletedKey() {
        //given
        final KeyVaultFake underTest = createUnderTest();
        insertMultipleVersionsOfSameKey(underTest, KEY_NAME_1);
        insertMultipleVersionsOfSameKey(underTest, KEY_NAME_2);
        insertMultipleVersionsOfSameKey(underTest, KEY_NAME_3);
        underTest.delete(UNVERSIONED_KEY_ENTITY_ID_1);
        Assertions.assertTrue(underTest.getDeletedEntities().containsName(KEY_NAME_1));

        //when
        underTest.purge(UNVERSIONED_KEY_ENTITY_ID_1);

        //then
        Assertions.assertFalse(underTest.getEntities().containsName(KEY_NAME_1));
        Assertions.assertFalse(underTest.getDeletedEntities().containsName(KEY_NAME_1));
    }

    @Test
    void testPurgeShouldThrowExceptionWhenCalledWithNullKey() {
        //given
        final KeyVaultFake underTest = createUnderTest();

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.purge(null));

        //then + exception
    }

    private KeyVaultFake createUnderTest() {
        final KeyVaultFake underTest = new VaultFakeImpl(HTTPS_LOCALHOST,
                RecoveryLevel.RECOVERABLE_AND_PURGEABLE,
                RecoveryLevel.MAX_RECOVERABLE_DAYS_INCLUSIVE).keyVaultFake();
        Assertions.assertInstanceOf(KeyVaultFakeImpl.class, underTest);
        return underTest;
    }

    private List<VersionedKeyEntityId> insertMultipleVersionsOfSameKey(final KeyVaultFake underTest, final String keyName) {
        return IntStream.range(0, COUNT)
                .mapToObj(i -> underTest.createEcKeyVersion(keyName, EC_KEY_CREATION_INPUT))
                .collect(Collectors.toList());
    }
}
