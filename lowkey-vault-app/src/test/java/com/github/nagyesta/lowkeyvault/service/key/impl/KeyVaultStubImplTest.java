package com.github.nagyesta.lowkeyvault.service.key.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyCurveName;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyOperation;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.service.KeyEntityId;
import com.github.nagyesta.lowkeyvault.service.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.exception.NotFoundException;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyAesKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.vault.impl.VaultStubImpl;
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
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOWKEY_VAULT;

class KeyVaultStubImplTest {

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

    @SuppressWarnings("ConstantConditions")
    @Test
    void testConstructorShouldThrowExceptionWhenCalledWithNull() {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> new KeyVaultStubImpl(null));

        //then + exception
    }

    @Test
    void testGetKeyVersionsShouldReturnAllKeyVersionsInChronologicalOrderWhenFound() {
        //given
        final KeyVaultStubImpl underTest = new KeyVaultStubImpl(new VaultStubImpl(HTTPS_LOCALHOST));
        final List<String> expected = insertMultipleVersionsOfSameKey(underTest).stream()
                .map(KeyEntityId::version)
                .collect(Collectors.toList());
        underTest.createKeyVersion(KEY_NAME_2, EC_KEY_CREATION_INPUT);
        underTest.createKeyVersion(KEY_NAME_3, EC_KEY_CREATION_INPUT);

        final KeyEntityId keyEntityId = new KeyEntityId(HTTPS_LOCALHOST, KEY_NAME_1, null);

        //when
        final Deque<String> actual = underTest.getVersions(keyEntityId);

        //then
        Assertions.assertIterableEquals(expected, actual);
    }

    @Test
    void testGetKeyVersionsShouldThrowExceptionWhenVaultUriDoesNotMatch() {
        //given
        final KeyVaultStubImpl underTest = new KeyVaultStubImpl(new VaultStubImpl(HTTPS_LOCALHOST));
        insertMultipleVersionsOfSameKey(underTest);

        final KeyEntityId keyEntityId = new KeyEntityId(HTTPS_LOWKEY_VAULT, KEY_NAME_1, null);

        //when
        Assertions.assertThrows(NotFoundException.class, () -> underTest.getVersions(keyEntityId));

        //then + exception
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testGetKeyVersionsShouldThrowExceptionWhenKeyIdIsNull() {
        //given
        final KeyVaultStubImpl underTest = new KeyVaultStubImpl(new VaultStubImpl(HTTPS_LOCALHOST));
        insertMultipleVersionsOfSameKey(underTest);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.getVersions(null));

        //then + exception
    }

    @Test
    void testGetKeyVersionsShouldThrowExceptionWhenNotFound() {
        //given
        final KeyVaultStubImpl underTest = new KeyVaultStubImpl(new VaultStubImpl(HTTPS_LOCALHOST));
        insertMultipleVersionsOfSameKey(underTest);

        final KeyEntityId keyEntityId = new KeyEntityId(HTTPS_LOCALHOST, KEY_NAME_2, null);

        //when
        Assertions.assertThrows(NotFoundException.class, () -> underTest.getVersions(keyEntityId));

        //then + exception
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testGetLatestVersionOfEntityShouldThrowExceptionWhenKeyIdIsNull() {
        //given
        final KeyVaultStubImpl underTest = new KeyVaultStubImpl(new VaultStubImpl(HTTPS_LOCALHOST));

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.getLatestVersionOfEntity(null));

        //then + exception
    }

    @Test
    void testGetLatestVersionOfKeyShouldReturnAllKeyVersionsInChronologicalOrderWhenFound() {
        //given
        final KeyVaultStubImpl underTest = new KeyVaultStubImpl(new VaultStubImpl(HTTPS_LOCALHOST));
        final String expected = insertMultipleVersionsOfSameKey(underTest).stream()
                .map(KeyEntityId::version)
                .skip(COUNT - 1)
                .findFirst().orElse(null);
        underTest.createKeyVersion(KEY_NAME_2, EC_KEY_CREATION_INPUT);
        underTest.createKeyVersion(KEY_NAME_3, EC_KEY_CREATION_INPUT);

        final KeyEntityId keyEntityId = new KeyEntityId(HTTPS_LOCALHOST, KEY_NAME_1, null);

        //when
        final VersionedKeyEntityId actual = underTest.getLatestVersionOfEntity(keyEntityId);

        //then
        Assertions.assertEquals(expected, actual.version());
    }

    @ParameterizedTest
    @MethodSource("genericKeyCreateInputProvider")
    void testCreateKeyVersionShouldThrowExceptionWhenCalledWithNull(final String name, final KeyCreationInput<?> input) {
        //given
        final KeyVaultStubImpl underTest = new KeyVaultStubImpl(new VaultStubImpl(HTTPS_LOCALHOST));

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
        final KeyVaultStubImpl underTest = new KeyVaultStubImpl(new VaultStubImpl(HTTPS_LOCALHOST));
        final RsaKeyCreationInput input = null;

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.createKeyVersion(keyName, input));

        //then + exception
    }

    @SuppressWarnings("ConstantConditions")
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = KEY_NAME_1)
    void testCreateKeyVersionShouldThrowExceptionWhenCalledWithNullEc(final String keyName) {
        //given
        final KeyVaultStubImpl underTest = new KeyVaultStubImpl(new VaultStubImpl(HTTPS_LOCALHOST));
        final EcKeyCreationInput input = null;

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.createKeyVersion(keyName, input));

        //then + exception
    }

    @SuppressWarnings("ConstantConditions")
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = KEY_NAME_1)
    void testCreateKeyVersionShouldThrowExceptionWhenCalledWithNullOct(final String keyName) {
        //given
        final KeyVaultStubImpl underTest = new KeyVaultStubImpl(new VaultStubImpl(HTTPS_LOCALHOST));
        final OctKeyCreationInput input = null;

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.createKeyVersion(keyName, input));

        //then + exception
    }

    @Test
    void testCreateKeyVersionShouldReturnIdWhenCalledWithValidRsaParameter() {
        //given
        final KeyVaultStubImpl underTest = new KeyVaultStubImpl(new VaultStubImpl(HTTPS_LOCALHOST));
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
        final KeyVaultStubImpl underTest = new KeyVaultStubImpl(new VaultStubImpl(HTTPS_LOCALHOST));
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
        final KeyVaultStubImpl underTest = new KeyVaultStubImpl(new VaultStubImpl(HTTPS_LOCALHOST));
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
        final KeyVaultStubImpl underTest = new KeyVaultStubImpl(new VaultStubImpl(HTTPS_LOCALHOST));
        final OctKeyCreationInput input = new OctKeyCreationInput(KeyType.OCT_HSM, null);
        final VersionedKeyEntityId keyEntityId = underTest.createKeyVersion(KEY_NAME_1, input);

        //when
        underTest.setKeyOperations(keyEntityId, list);
        final ReadOnlyKeyVaultKeyEntity actual = underTest.getEntity(keyEntityId);

        //then
        Assertions.assertIterableEquals(list, actual.getOperations());
    }

    @ParameterizedTest
    @MethodSource("invalidKeyOperationsProvider")
    void testSetKeyOperationsShouldThrowExceptionWhenCalledWithInvalidValues(
            final VersionedKeyEntityId keyEntityId, final List<KeyOperation> list) {
        //given
        final KeyVaultStubImpl underTest = new KeyVaultStubImpl(new VaultStubImpl(HTTPS_LOCALHOST));

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.setKeyOperations(keyEntityId, list));

        //then + exception
    }

    @Test
    void testClearTagsShouldClearPreviouslySetTagsWhenCalledOnValidKey() {
        //given
        final KeyVaultStubImpl underTest = new KeyVaultStubImpl(new VaultStubImpl(HTTPS_LOCALHOST));
        final OctKeyCreationInput input = new OctKeyCreationInput(KeyType.OCT_HSM, null);
        final VersionedKeyEntityId keyEntityId = underTest.createKeyVersion(KEY_NAME_1, input);

        //when
        underTest.addTags(keyEntityId, TAGS_TWO_KEYS);
        final ReadOnlyKeyVaultKeyEntity check = underTest.getEntity(keyEntityId);
        Assumptions.assumeTrue(check.getTags().containsKey(KEY_1));
        Assumptions.assumeTrue(check.getTags().containsKey(KEY_2));
        underTest.clearTags(keyEntityId);

        final ReadOnlyKeyVaultKeyEntity actual = underTest.getEntity(keyEntityId);

        //then
        Assertions.assertEquals(Collections.emptyMap(), actual.getTags());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testClearTagsShouldThrowExceptionWhenCalledWithNullKey() {
        //given
        final KeyVaultStubImpl underTest = new KeyVaultStubImpl(new VaultStubImpl(HTTPS_LOCALHOST));

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.clearTags(null));

        //then + exception
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testAddTagsShouldThrowExceptionWhenCalledWithNullKey() {
        //given
        final KeyVaultStubImpl underTest = new KeyVaultStubImpl(new VaultStubImpl(HTTPS_LOCALHOST));

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.addTags(null, TAGS_EMPTY));

        //then + exception
    }

    @Test
    void testSetEnabledShouldReplacePreviouslySetValueWhenCalledOnValidKey() {
        //given
        final KeyVaultStubImpl underTest = new KeyVaultStubImpl(new VaultStubImpl(HTTPS_LOCALHOST));
        final OctKeyCreationInput input = new OctKeyCreationInput(KeyType.OCT_HSM, null);
        final VersionedKeyEntityId keyEntityId = underTest.createKeyVersion(KEY_NAME_1, input);
        final ReadOnlyKeyVaultKeyEntity check = underTest.getEntity(keyEntityId);
        Assertions.assertFalse(check.isEnabled());

        //when
        underTest.setEnabled(keyEntityId, true);
        final ReadOnlyKeyVaultKeyEntity actual = underTest.getEntity(keyEntityId);

        //then
        Assertions.assertTrue(actual.isEnabled());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testSetEnabledShouldThrowExceptionWhenCalledWithNullKey() {
        //given
        final KeyVaultStubImpl underTest = new KeyVaultStubImpl(new VaultStubImpl(HTTPS_LOCALHOST));

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.setEnabled(null, true));

        //then + exception
    }

    @Test
    void testSetExpiryShouldReplacePreviouslySetValueWhenCalledOnValidKey() {
        //given
        final KeyVaultStubImpl underTest = new KeyVaultStubImpl(new VaultStubImpl(HTTPS_LOCALHOST));
        final OctKeyCreationInput input = new OctKeyCreationInput(KeyType.OCT_HSM, null);
        final VersionedKeyEntityId keyEntityId = underTest.createKeyVersion(KEY_NAME_1, input);
        final ReadOnlyKeyVaultKeyEntity check = underTest.getEntity(keyEntityId);
        Assertions.assertTrue(check.getExpiry().isEmpty());
        Assertions.assertTrue(check.getNotBefore().isEmpty());

        //when
        underTest.setExpiry(keyEntityId, TIME_10_MINUTES_AGO, TIME_IN_10_MINUTES);
        final ReadOnlyKeyVaultKeyEntity actual = underTest.getEntity(keyEntityId);

        //then
        Assertions.assertTrue(actual.getNotBefore().isPresent());
        Assertions.assertTrue(actual.getExpiry().isPresent());
        Assertions.assertEquals(TIME_10_MINUTES_AGO, actual.getNotBefore().get());
        Assertions.assertEquals(TIME_IN_10_MINUTES, actual.getExpiry().get());
    }

    @Test
    void testSetExpiryShouldReplacePreviouslySetValueWhenCalledOnValidKeyAndNotBeforeOnly() {
        //given
        final KeyVaultStubImpl underTest = new KeyVaultStubImpl(new VaultStubImpl(HTTPS_LOCALHOST));
        final OctKeyCreationInput input = new OctKeyCreationInput(KeyType.OCT_HSM, null);
        final VersionedKeyEntityId keyEntityId = underTest.createKeyVersion(KEY_NAME_1, input);
        final ReadOnlyKeyVaultKeyEntity check = underTest.getEntity(keyEntityId);
        Assertions.assertTrue(check.getExpiry().isEmpty());
        Assertions.assertTrue(check.getNotBefore().isEmpty());

        //when
        underTest.setExpiry(keyEntityId, TIME_10_MINUTES_AGO, null);
        final ReadOnlyKeyVaultKeyEntity actual = underTest.getEntity(keyEntityId);

        //then
        Assertions.assertTrue(actual.getNotBefore().isPresent());
        Assertions.assertTrue(actual.getExpiry().isEmpty());
        Assertions.assertEquals(TIME_10_MINUTES_AGO, actual.getNotBefore().get());
    }

    @Test
    void testSetExpiryShouldReplacePreviouslySetValueWhenCalledOnValidKeyAndExpiryOnly() {
        //given
        final KeyVaultStubImpl underTest = new KeyVaultStubImpl(new VaultStubImpl(HTTPS_LOCALHOST));
        final OctKeyCreationInput input = new OctKeyCreationInput(KeyType.OCT_HSM, null);
        final VersionedKeyEntityId keyEntityId = underTest.createKeyVersion(KEY_NAME_1, input);
        final ReadOnlyKeyVaultKeyEntity check = underTest.getEntity(keyEntityId);
        Assertions.assertTrue(check.getExpiry().isEmpty());
        Assertions.assertTrue(check.getNotBefore().isEmpty());

        //when
        underTest.setExpiry(keyEntityId, null, TIME_IN_10_MINUTES);
        final ReadOnlyKeyVaultKeyEntity actual = underTest.getEntity(keyEntityId);

        //then
        Assertions.assertTrue(actual.getNotBefore().isEmpty());
        Assertions.assertTrue(actual.getExpiry().isPresent());
        Assertions.assertEquals(TIME_IN_10_MINUTES, actual.getExpiry().get());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testSetExpiryShouldThrowExceptionWhenCalledWithNullKey() {
        //given
        final KeyVaultStubImpl underTest = new KeyVaultStubImpl(new VaultStubImpl(HTTPS_LOCALHOST));

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> underTest.setExpiry(null, TIME_10_MINUTES_AGO, TIME_IN_10_MINUTES));

        //then + exception
    }

    @Test
    void testSetExpiryShouldThrowExceptionWhenCalledWithNegativeTimeDuration() {
        //given
        final KeyVaultStubImpl underTest = new KeyVaultStubImpl(new VaultStubImpl(HTTPS_LOCALHOST));
        final OctKeyCreationInput input = new OctKeyCreationInput(KeyType.OCT_HSM, null);
        final VersionedKeyEntityId keyEntityId = underTest.createKeyVersion(KEY_NAME_1, input);

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> underTest.setExpiry(keyEntityId, TIME_IN_10_MINUTES, TIME_10_MINUTES_AGO));

        //then + exception
    }

    @Test
    void testSetRecoveryShouldReplacePreviouslySetValueWhenCalledOnValidKey() {
        //given
        final KeyVaultStubImpl underTest = new KeyVaultStubImpl(new VaultStubImpl(HTTPS_LOCALHOST));
        final OctKeyCreationInput input = new OctKeyCreationInput(KeyType.OCT_HSM, null);
        final VersionedKeyEntityId keyEntityId = underTest.createKeyVersion(KEY_NAME_1, input);
        final ReadOnlyKeyVaultKeyEntity check = underTest.getEntity(keyEntityId);
        Assertions.assertNull(check.getRecoveryLevel());
        Assertions.assertNull(check.getRecoverableDays());

        //when
        underTest.setRecovery(keyEntityId, RecoveryLevel.CUSTOMIZED_RECOVERABLE, DAYS);
        final ReadOnlyKeyVaultKeyEntity actual = underTest.getEntity(keyEntityId);

        //then
        Assertions.assertEquals(DAYS, actual.getRecoverableDays());
        Assertions.assertEquals(RecoveryLevel.CUSTOMIZED_RECOVERABLE, actual.getRecoveryLevel());
    }

    @Test
    void testSetRecoveryShouldThrowExceptionWhenCalledWithInvalidData() {
        //given
        final KeyVaultStubImpl underTest = new KeyVaultStubImpl(new VaultStubImpl(HTTPS_LOCALHOST));
        final OctKeyCreationInput input = new OctKeyCreationInput(KeyType.OCT_HSM, null);
        final VersionedKeyEntityId keyEntityId = underTest.createKeyVersion(KEY_NAME_1, input);

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> underTest.setRecovery(keyEntityId, RecoveryLevel.PURGEABLE, DAYS));

        //then + exception
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testSetRecoveryShouldThrowExceptionWhenCalledWithNullRecoveryLevel() {
        //given
        final KeyVaultStubImpl underTest = new KeyVaultStubImpl(new VaultStubImpl(HTTPS_LOCALHOST));
        final OctKeyCreationInput input = new OctKeyCreationInput(KeyType.OCT_HSM, null);
        final VersionedKeyEntityId keyEntityId = underTest.createKeyVersion(KEY_NAME_1, input);

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> underTest.setRecovery(keyEntityId, null, DAYS));

        //then + exception
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testSetRecoveryShouldThrowExceptionWhenCalledWithNullKey() {
        //given
        final KeyVaultStubImpl underTest = new KeyVaultStubImpl(new VaultStubImpl(HTTPS_LOCALHOST));

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> underTest.setRecovery(null, RecoveryLevel.CUSTOMIZED_RECOVERABLE, DAYS));

        //then + exception
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testGetEntityShouldThrowExceptionWhenCalledWithNullKey() {
        //given
        final KeyVaultStubImpl underTest = new KeyVaultStubImpl(new VaultStubImpl(HTTPS_LOCALHOST));

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> underTest.getEntity(null, null));

        //then + exception
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testGetEntityShouldThrowExceptionWhenCalledWithNullType() {
        //given
        final KeyVaultStubImpl underTest = new KeyVaultStubImpl(new VaultStubImpl(HTTPS_LOCALHOST));
        final OctKeyCreationInput input = new OctKeyCreationInput(KeyType.OCT_HSM, null);
        final VersionedKeyEntityId keyEntityId = underTest.createKeyVersion(KEY_NAME_1, input);

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> underTest.getEntity(keyEntityId, null));

        //then + exception
    }

    @Test
    void testGetEntityShouldReturnValueWhenCalledWithExistingKeyAndValidType() {
        //given
        final KeyVaultStubImpl underTest = new KeyVaultStubImpl(new VaultStubImpl(HTTPS_LOCALHOST));
        final OctKeyCreationInput input = new OctKeyCreationInput(KeyType.OCT_HSM, null);
        final VersionedKeyEntityId keyEntityId = underTest.createKeyVersion(KEY_NAME_1, input);

        //when
        final ReadOnlyAesKeyVaultKeyEntity actual = underTest.getEntity(keyEntityId, ReadOnlyAesKeyVaultKeyEntity.class);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(keyEntityId.asUri(), actual.getUri());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testRawGetEntityShouldThrowExceptionWhenCalledWithNullKey() {
        //given
        final KeyVaultStubImpl underTest = new KeyVaultStubImpl(new VaultStubImpl(HTTPS_LOCALHOST));

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.getEntity(null));

        //then + exception
    }

    private List<VersionedKeyEntityId> insertMultipleVersionsOfSameKey(final KeyVaultStubImpl underTest) {
        return IntStream.range(0, COUNT)
                .mapToObj(i -> underTest.createKeyVersion(KEY_NAME_1, EC_KEY_CREATION_INPUT))
                .collect(Collectors.toList());
    }
}
