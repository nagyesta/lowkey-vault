package com.github.nagyesta.lowkeyvault.model.v7_2.key;

import com.github.nagyesta.lowkeyvault.mapper.common.registry.KeyConverterRegistry;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72BackupConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72PropertiesModelConverter;
import com.github.nagyesta.lowkeyvault.model.common.backup.KeyBackupListItem;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyCurveName;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.JsonWebKeyImportRequest;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.impl.AesKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.impl.EcKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.impl.RsaKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.util.KeyGenUtil;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

import static com.github.nagyesta.lowkeyvault.TestConstants.KEY_1;
import static com.github.nagyesta.lowkeyvault.TestConstants.VALUE_1;
import static com.github.nagyesta.lowkeyvault.TestConstantsKeys.VERSIONED_KEY_ENTITY_ID_1_VERSION_1;
import static org.mockito.Mockito.*;

class KeyEntityToV72BackupConverterTest {

    private static final KeyPropertiesModel KEY_PROPERTIES_MODEL = new KeyPropertiesModel();
    private KeyEntityToV72BackupConverter underTest;
    @Mock
    private VaultFake vaultFake;
    @Mock
    private KeyEntityToV72PropertiesModelConverter propertiesModelConverter;
    @Mock
    private KeyConverterRegistry registry;
    private AutoCloseable openMocks;

    @BeforeEach
    void setUp() {
        openMocks = MockitoAnnotations.openMocks(this);
        underTest = new KeyEntityToV72BackupConverter(registry);
        when(registry.propertiesConverter(anyString())).thenReturn(propertiesModelConverter);
        when(propertiesModelConverter.convert(any(ReadOnlyKeyVaultKeyEntity.class), any(URI.class))).thenReturn(KEY_PROPERTIES_MODEL);
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testConstructorShouldThrowExceptionWhenCalledWithNull() {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> new KeyEntityToV72BackupConverter(null));

        //then + exception
    }

    @Test
    void testConvertShouldConvertPopulatedFieldsWhenCalledWithMinimalRsaInput() {
        //given
        final var keySize = KeyType.RSA.getValidKeyParameters(Integer.class).first();
        final var keyPair = KeyGenUtil.generateRsa(keySize, null);
        final var input = new RsaKeyVaultKeyEntity(
                VERSIONED_KEY_ENTITY_ID_1_VERSION_1, vaultFake, keyPair, keySize, false);

        //when
        final var actual = underTest.convert(input);

        //then
        Assertions.assertNotNull(actual);
        final var keyMaterial = actual.getKeyMaterial();
        assertCommonKeyPropertiesAreEqual(input, keyMaterial);
        assertRsaPropertiesAreEqual(input, keyMaterial);
        assertMinimalPropertiesPopulated(actual);
        assertIdsEqual(input.getId(), actual);
        verify(propertiesModelConverter).convert(any(ReadOnlyKeyVaultKeyEntity.class), any(URI.class));
        verifyNoMoreInteractions(propertiesModelConverter);
    }

    @Test
    void testConvertShouldConvertPopulatedFieldsWhenCalledWithMinimalAesInput() {
        //given
        final var keySize = KeyType.OCT.getValidKeyParameters(Integer.class).first();
        final var secretKey = KeyGenUtil.generateAes(keySize);
        final var input = new AesKeyVaultKeyEntity(
                VERSIONED_KEY_ENTITY_ID_1_VERSION_1, vaultFake, secretKey, keySize, true);

        //when
        final var actual = underTest.convert(input);

        //then
        Assertions.assertNotNull(actual);
        final var keyMaterial = actual.getKeyMaterial();
        assertCommonKeyPropertiesAreEqual(input, keyMaterial);
        assertOctPropertiesAreEqual(input, keyMaterial);
        assertMinimalPropertiesPopulated(actual);
        assertIdsEqual(input.getId(), actual);
        verify(propertiesModelConverter).convert(any(ReadOnlyKeyVaultKeyEntity.class), any(URI.class));
        verifyNoMoreInteractions(propertiesModelConverter);
    }

    @Test
    void testConvertShouldConvertPopulatedFieldsWhenCalledWithMinimalEcInput() {
        //given
        final var keyPair = KeyGenUtil.generateEc(KeyCurveName.P_256);
        final var input = new EcKeyVaultKeyEntity(
                VERSIONED_KEY_ENTITY_ID_1_VERSION_1, vaultFake, keyPair, KeyCurveName.P_256, true);

        //when
        final var actual = underTest.convert(input);

        //then
        Assertions.assertNotNull(actual);
        final var keyMaterial = actual.getKeyMaterial();
        assertCommonKeyPropertiesAreEqual(input, keyMaterial);
        assertEcPropertiesAreEqual(input, keyMaterial);
        assertMinimalPropertiesPopulated(actual);
        assertIdsEqual(input.getId(), actual);
        verify(propertiesModelConverter).convert(any(ReadOnlyKeyVaultKeyEntity.class), any(URI.class));
        verifyNoMoreInteractions(propertiesModelConverter);
    }

    @Test
    void testConvertShouldConvertAllFieldsWhenCalledWithFullyPopulatedInput() {
        //given
        final var tagMap = Map.of(KEY_1, VALUE_1);
        final var keyPair = KeyGenUtil.generateEc(KeyCurveName.P_256);
        final var input = new EcKeyVaultKeyEntity(
                VERSIONED_KEY_ENTITY_ID_1_VERSION_1, vaultFake, keyPair, KeyCurveName.P_256, true);
        input.setTags(tagMap);
        input.setManaged(true);

        //when
        final var actual = underTest.convert(input);

        //then
        Assertions.assertNotNull(actual);
        final var keyMaterial = actual.getKeyMaterial();
        assertCommonKeyPropertiesAreEqual(input, keyMaterial);
        assertEcPropertiesAreEqual(input, keyMaterial);
        Assertions.assertSame(KEY_PROPERTIES_MODEL, actual.getAttributes());
        Assertions.assertEquals(tagMap, actual.getTags());
        Assertions.assertTrue(actual.isManaged());
        assertIdsEqual(input.getId(), actual);
        verify(propertiesModelConverter).convert(any(ReadOnlyKeyVaultKeyEntity.class), any(URI.class));
        verifyNoMoreInteractions(propertiesModelConverter);
    }

    private void assertRsaPropertiesAreEqual(final RsaKeyVaultKeyEntity input, final JsonWebKeyImportRequest keyMaterial) {
        Assertions.assertArrayEquals(input.getN(), keyMaterial.getN());
        Assertions.assertArrayEquals(input.getE(), keyMaterial.getE());
        Assertions.assertArrayEquals(input.getD(), keyMaterial.getD());
        Assertions.assertArrayEquals(input.getDp(), keyMaterial.getDp());
        Assertions.assertArrayEquals(input.getDq(), keyMaterial.getDq());
        Assertions.assertArrayEquals(input.getP(), keyMaterial.getP());
        Assertions.assertArrayEquals(input.getQ(), keyMaterial.getQ());
        Assertions.assertArrayEquals(input.getQi(), keyMaterial.getQi());
    }

    private void assertOctPropertiesAreEqual(final AesKeyVaultKeyEntity input, final JsonWebKeyImportRequest keyMaterial) {
        Assertions.assertArrayEquals(input.getK(), keyMaterial.getK());
    }

    private void assertEcPropertiesAreEqual(final EcKeyVaultKeyEntity input, final JsonWebKeyImportRequest keyMaterial) {
        Assertions.assertArrayEquals(input.getD(), keyMaterial.getD());
        Assertions.assertArrayEquals(input.getX(), keyMaterial.getX());
        Assertions.assertArrayEquals(input.getY(), keyMaterial.getY());
    }

    private void assertMinimalPropertiesPopulated(final KeyBackupListItem actual) {
        Assertions.assertSame(KEY_PROPERTIES_MODEL, actual.getAttributes());
        Assertions.assertEquals(Collections.emptyMap(), actual.getTags());
        Assertions.assertFalse(actual.isManaged());
    }

    private void assertCommonKeyPropertiesAreEqual(final ReadOnlyKeyVaultKeyEntity input, final JsonWebKeyImportRequest keyMaterial) {
        Assertions.assertNotNull(keyMaterial);
        Assertions.assertEquals(input.getKeyType(), keyMaterial.getKeyType());
        Assertions.assertEquals(input.getOperations(), keyMaterial.getKeyOps());
    }

    private void assertIdsEqual(final VersionedKeyEntityId input, final KeyBackupListItem actual) {
        Assertions.assertEquals(input.vault(), actual.getVaultBaseUri());
        Assertions.assertEquals(input.id(), actual.getId());
        Assertions.assertEquals(input.version(), actual.getVersion());
    }
}
