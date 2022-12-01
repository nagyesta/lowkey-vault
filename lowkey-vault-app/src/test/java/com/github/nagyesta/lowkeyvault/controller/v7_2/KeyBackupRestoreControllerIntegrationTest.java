package com.github.nagyesta.lowkeyvault.controller.v7_2;

import com.github.nagyesta.abortmission.booster.jupiter.annotation.LaunchAbortArmed;
import com.github.nagyesta.lowkeyvault.TestConstantsUri;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72BackupConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72ModelConverter;
import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.*;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyCurveName;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyOperation;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.JsonWebKeyImportRequest;
import com.github.nagyesta.lowkeyvault.service.exception.NotFoundException;
import com.github.nagyesta.lowkeyvault.service.key.KeyVaultFake;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.impl.EcKeyCreationInput;
import com.github.nagyesta.lowkeyvault.service.key.util.KeyGenUtil;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.security.KeyPair;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstants.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsKeys.*;
import static org.mockito.Mockito.mock;

@LaunchAbortArmed
@SpringBootTest
class KeyBackupRestoreControllerIntegrationTest {

    @Autowired
    @Qualifier("KeyBackupRestoreControllerV72")
    private KeyBackupRestoreController underTest;
    @Autowired
    private VaultService vaultService;
    private URI uri;

    public static Stream<Arguments> nullProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null, null))
                .add(Arguments.of(mock(KeyEntityToV72ModelConverter.class), null, null))
                .add(Arguments.of(null, mock(KeyEntityToV72BackupConverter.class), null))
                .add(Arguments.of(null, null, mock(VaultService.class)))
                .add(Arguments.of(null, mock(KeyEntityToV72BackupConverter.class), null))
                .add(Arguments.of(mock(KeyEntityToV72ModelConverter.class), null, mock(VaultService.class)))
                .add(Arguments.of(mock(KeyEntityToV72ModelConverter.class), mock(KeyEntityToV72BackupConverter.class), null))
                .build();
    }

    @BeforeEach
    void setUp() {
        final String name = UUID.randomUUID().toString();
        uri = URI.create("https://" + name + ".localhost");
        vaultService.create(uri, RecoveryLevel.RECOVERABLE_AND_PURGEABLE, RecoveryLevel.MAX_RECOVERABLE_DAYS_INCLUSIVE, null);
    }

    @AfterEach
    void tearDown() {
        vaultService.delete(uri);
        vaultService.purge(uri);
    }

    @ParameterizedTest
    @MethodSource("nullProvider")
    void testConstructorShouldThrowExceptionWhenCalledWithNulls(
            final KeyEntityToV72ModelConverter modelConverter,
            final KeyEntityToV72BackupConverter backupConverter,
            final VaultService vaultService) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new KeyBackupRestoreController(modelConverter, backupConverter, vaultService));

        //then + exception
    }

    @Test
    void testRestoreEntityShouldRestoreASingleKeyWhenCalledWithValidInput() {
        //given
        final KeyBackupModel backupModel = new KeyBackupModel();
        backupModel.setValue(new KeyBackupList());
        final KeyPair expectedKey = addVersionToList(uri, KEY_NAME_1, KEY_VERSION_1, backupModel, TAGS_THREE_KEYS);

        //when
        final ResponseEntity<KeyVaultKeyModel> actual = underTest.restore(uri, backupModel);

        //then
        Assertions.assertNotNull(actual);
        final KeyVaultKeyModel actualBody = actual.getBody();
        Assertions.assertNotNull(actualBody);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertRestoredKeyMatchesExpectations(actualBody, (ECPublicKey) expectedKey.getPublic(), KEY_VERSION_1, TAGS_THREE_KEYS);
    }

    @Test
    void testRestoreEntityShouldRestoreThreeKeysWhenCalledWithValidInput() {
        //given
        final KeyBackupModel backupModel = new KeyBackupModel();
        backupModel.setValue(new KeyBackupList());
        addVersionToList(uri, KEY_NAME_1, KEY_VERSION_1, backupModel, null);
        addVersionToList(uri, KEY_NAME_1, KEY_VERSION_2, backupModel, TAGS_THREE_KEYS);
        final KeyPair expectedKey = addVersionToList(uri, KEY_NAME_1, KEY_VERSION_3, backupModel, TAGS_EMPTY);

        //when
        final ResponseEntity<KeyVaultKeyModel> actual = underTest.restore(uri, backupModel);

        //then
        Assertions.assertNotNull(actual);
        final KeyVaultKeyModel actualBody = actual.getBody();
        Assertions.assertNotNull(actualBody);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertRestoredKeyMatchesExpectations(actualBody, (ECPublicKey) expectedKey.getPublic(), KEY_VERSION_3, TAGS_EMPTY);
    }

    @Test
    void testRestoreEntityShouldThrowExceptionWhenCalledWithMoreThanOneUris() {
        //given
        final KeyBackupModel backupModel = new KeyBackupModel();
        backupModel.setValue(new KeyBackupList());
        addVersionToList(uri, KEY_NAME_1, KEY_VERSION_1, backupModel, null);
        addVersionToList(TestConstantsUri.HTTPS_DEFAULT_LOWKEY_VAULT, KEY_NAME_1, KEY_VERSION_2, backupModel, TAGS_THREE_KEYS);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.restore(uri, backupModel));

        //then + exception
    }

    @Test
    void testRestoreEntityShouldThrowExceptionWhenCalledWithMoreThanOneNames() {
        //given
        final KeyBackupModel backupModel = new KeyBackupModel();
        backupModel.setValue(new KeyBackupList());
        addVersionToList(uri, KEY_NAME_1, KEY_VERSION_1, backupModel, null);
        addVersionToList(uri, KEY_NAME_2, KEY_VERSION_2, backupModel, TAGS_THREE_KEYS);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.restore(uri, backupModel));

        //then + exception
    }

    @Test
    void testRestoreEntityShouldThrowExceptionWhenCalledWithUnknownUri() {
        //given
        final KeyBackupModel backupModel = new KeyBackupModel();
        backupModel.setValue(new KeyBackupList());
        addVersionToList(URI.create("https://uknknown.uri"), KEY_NAME_1, KEY_VERSION_1, backupModel, null);

        //when
        Assertions.assertThrows(NotFoundException.class, () -> underTest.restore(uri, backupModel));

        //then + exception
    }

    @Test
    void testRestoreEntityShouldThrowExceptionWhenNameMatchesActiveKey() {
        //given
        final KeyBackupModel backupModel = new KeyBackupModel();
        backupModel.setValue(new KeyBackupList());
        addVersionToList(uri, KEY_NAME_1, KEY_VERSION_1, backupModel, TAGS_EMPTY);
        addVersionToList(uri, KEY_NAME_1, KEY_VERSION_2, backupModel, TAGS_ONE_KEY);
        vaultService.findByUri(uri).keyVaultFake()
                .createKeyVersion(KEY_NAME_1, new EcKeyCreationInput(KeyType.EC, KeyCurveName.P_256));

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.restore(uri, backupModel));

        //then + exception
    }

    @Test
    void testRestoreEntityShouldThrowExceptionWhenNameMatchesDeletedKey() {
        //given
        final KeyBackupModel backupModel = new KeyBackupModel();
        backupModel.setValue(new KeyBackupList());
        addVersionToList(uri, KEY_NAME_1, KEY_VERSION_1, backupModel, TAGS_EMPTY);
        addVersionToList(uri, KEY_NAME_1, KEY_VERSION_2, backupModel, TAGS_ONE_KEY);
        final KeyVaultFake vaultFake = vaultService.findByUri(uri).keyVaultFake();
        final VersionedKeyEntityId keyVersion = vaultFake
                .createKeyVersion(KEY_NAME_1, new EcKeyCreationInput(KeyType.EC, KeyCurveName.P_256));
        vaultFake.delete(keyVersion);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.restore(uri, backupModel));

        //then + exception
    }

    @Test
    void testBackupEntityShouldReturnTheOriginalBackupModelWhenCalledAfterRestoreEntity() {
        //given
        final KeyBackupModel backupModel = new KeyBackupModel();
        backupModel.setValue(new KeyBackupList());
        addVersionToList(uri, KEY_NAME_1, KEY_VERSION_1, backupModel, TAGS_EMPTY);
        addVersionToList(uri, KEY_NAME_1, KEY_VERSION_2, backupModel, TAGS_ONE_KEY);
        underTest.restore(uri, backupModel);

        //when
        final ResponseEntity<KeyBackupModel> actual = underTest.backup(KEY_NAME_1, uri);

        //then
        Assertions.assertNotNull(actual);
        final KeyBackupModel actualBody = actual.getBody();
        Assertions.assertNotNull(actualBody);
        Assertions.assertEquals(backupModel, actualBody);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
    }

    private void assertRestoredKeyMatchesExpectations(
            final KeyVaultKeyModel actualBody, final ECPublicKey publicKey,
            final String version, final Map<String, String> expectedTags) {
        Assertions.assertEquals(new VersionedKeyEntityId(uri, KEY_NAME_1, version).asUri(uri).toString(), actualBody.getKey().getId());
        Assertions.assertEquals(KeyCurveName.P_256, actualBody.getKey().getCurveName());
        Assertions.assertEquals(KeyType.EC, actualBody.getKey().getKeyType());
        Assertions.assertIterableEquals(List.of(KeyOperation.SIGN, KeyOperation.VERIFY), actualBody.getKey().getKeyOps());
        Assertions.assertArrayEquals(publicKey.getW().getAffineX().toByteArray(), actualBody.getKey().getX());
        Assertions.assertArrayEquals(publicKey.getW().getAffineY().toByteArray(), actualBody.getKey().getY());
        //do not return private key material in response
        Assertions.assertNull(actualBody.getKey().getD());
        Assertions.assertEquals(TIME_10_MINUTES_AGO, actualBody.getAttributes().getCreatedOn());
        Assertions.assertEquals(NOW, actualBody.getAttributes().getUpdatedOn());
        Assertions.assertEquals(TIME_IN_10_MINUTES, actualBody.getAttributes().getNotBefore());
        Assertions.assertEquals(TIME_IN_10_MINUTES.plusDays(1), actualBody.getAttributes().getExpiresOn());
        Assertions.assertEquals(RecoveryLevel.RECOVERABLE_AND_PURGEABLE, actualBody.getAttributes().getRecoveryLevel());
        Assertions.assertEquals(RecoveryLevel.MAX_RECOVERABLE_DAYS_INCLUSIVE, actualBody.getAttributes().getRecoverableDays());
        Assertions.assertTrue(actualBody.getAttributes().isEnabled());
        Assertions.assertEquals(expectedTags, actualBody.getTags());
    }

    private KeyPair addVersionToList(final URI baseUri, final String name, final String version,
                                     final KeyBackupModel backupModel, final Map<String, String> tags) {
        final KeyPair keyPair = KeyGenUtil.generateEc(KeyCurveName.P_256);
        final JsonWebKeyImportRequest keyMaterial = new JsonWebKeyImportRequest();
        keyMaterial.setKeyType(KeyType.EC);
        keyMaterial.setCurveName(KeyCurveName.P_256);
        keyMaterial.setKeyOps(List.of(KeyOperation.SIGN, KeyOperation.VERIFY));
        keyMaterial.setD(((ECPrivateKey) keyPair.getPrivate()).getS().toByteArray());
        keyMaterial.setX(((ECPublicKey) keyPair.getPublic()).getW().getAffineX().toByteArray());
        keyMaterial.setY(((ECPublicKey) keyPair.getPublic()).getW().getAffineY().toByteArray());
        keyMaterial.setId(new VersionedKeyEntityId(baseUri, name, version).asUri(uri).toString());
        final KeyBackupListItem listItem = new KeyBackupListItem();
        listItem.setKeyMaterial(keyMaterial);
        listItem.setVaultBaseUri(baseUri);
        listItem.setId(name);
        listItem.setVersion(version);
        final KeyPropertiesModel propertiesModel = new KeyPropertiesModel();
        propertiesModel.setCreatedOn(TIME_10_MINUTES_AGO);
        propertiesModel.setUpdatedOn(NOW);
        propertiesModel.setNotBefore(TIME_IN_10_MINUTES);
        propertiesModel.setExpiresOn(TIME_IN_10_MINUTES.plusDays(1));
        propertiesModel.setRecoveryLevel(RecoveryLevel.RECOVERABLE_AND_PURGEABLE);
        propertiesModel.setRecoverableDays(RecoveryLevel.MAX_RECOVERABLE_DAYS_INCLUSIVE);
        listItem.setAttributes(propertiesModel);
        listItem.setTags(tags);
        final List<KeyBackupListItem> list = new ArrayList<>(backupModel.getValue().getVersions());
        list.add(listItem);
        backupModel.getValue().setVersions(list);
        return keyPair;
    }
}
