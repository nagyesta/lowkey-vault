package com.github.nagyesta.lowkeyvault.controller;

import com.github.nagyesta.lowkeyvault.model.common.backup.KeyBackupListItem;
import com.github.nagyesta.lowkeyvault.model.common.backup.KeyBackupModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyPropertiesModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyVaultKeyModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyCurveName;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyOperation;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.JsonWebKeyImportRequest;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.util.KeyGenUtil;
import org.junit.jupiter.api.Assertions;

import java.net.URI;
import java.security.KeyPair;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.github.nagyesta.lowkeyvault.TestConstants.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsKeys.KEY_NAME_1;

public abstract class BaseKeyBackupRestoreControllerIntegrationTest {

    @SuppressWarnings({"checkstyle:VisibilityModifier", "checkstyle:JavadocVariable"})
    protected URI uri;

    protected void assertRestoredKeyMatchesExpectations(
            final KeyVaultKeyModel actualBody, final ECPublicKey publicKey,
            final String version, final Map<String, String> expectedTags) {
        Assertions.assertEquals(new VersionedKeyEntityId(uri, KEY_NAME_1, version).asUri(uri).toString(), actualBody.getKey().getId());
        Assertions.assertEquals(KeyCurveName.P_256, actualBody.getKey().getCurveName());
        Assertions.assertEquals(KeyType.EC, actualBody.getKey().getKeyType());
        Assertions.assertIterableEquals(List.of(KeyOperation.SIGN, KeyOperation.VERIFY), actualBody.getKey().getKeyOps());
        final var expectedX = normalize(publicKey.getW().getAffineX().toByteArray(), KeyCurveName.P_256.getByteLength());
        Assertions.assertArrayEquals(expectedX, actualBody.getKey().getX());
        final var expectedY = normalize(publicKey.getW().getAffineY().toByteArray(), KeyCurveName.P_256.getByteLength());
        Assertions.assertArrayEquals(expectedY, actualBody.getKey().getY());
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

    protected KeyPair addVersionToList(final URI baseUri, final String name, final String version,
                                       final KeyBackupModel backupModel, final Map<String, String> tags) {
        final var keyPair = KeyGenUtil.generateEc(KeyCurveName.P_256);
        final var keyMaterial = new JsonWebKeyImportRequest();
        keyMaterial.setKeyType(KeyType.EC);
        keyMaterial.setCurveName(KeyCurveName.P_256);
        keyMaterial.setKeyOps(List.of(KeyOperation.SIGN, KeyOperation.VERIFY));
        keyMaterial.setD(((ECPrivateKey) keyPair.getPrivate()).getS().toByteArray());
        final var w = ((ECPublicKey) keyPair.getPublic()).getW();
        keyMaterial.setX(normalize(w.getAffineX().toByteArray(), KeyCurveName.P_256.getByteLength()));
        keyMaterial.setY(normalize(w.getAffineY().toByteArray(), KeyCurveName.P_256.getByteLength()));
        keyMaterial.setId(new VersionedKeyEntityId(baseUri, name, version).asUri(uri).toString());
        final var listItem = new KeyBackupListItem();
        listItem.setKeyMaterial(keyMaterial);
        listItem.setVaultBaseUri(baseUri);
        listItem.setId(name);
        listItem.setVersion(version);
        final var propertiesModel = new KeyPropertiesModel();
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

    private byte[] normalize(final byte[] bytes, final int expectedLength) {
        if (expectedLength < bytes.length) {
            return Arrays.copyOfRange(bytes, bytes.length - expectedLength, bytes.length);
        } else {
            return bytes;
        }
    }
}
