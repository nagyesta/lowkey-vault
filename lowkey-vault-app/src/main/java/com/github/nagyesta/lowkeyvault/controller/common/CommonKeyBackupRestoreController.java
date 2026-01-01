package com.github.nagyesta.lowkeyvault.controller.common;

import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72BackupConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72KeyItemModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72ModelConverter;
import com.github.nagyesta.lowkeyvault.model.common.backup.KeyBackupList;
import com.github.nagyesta.lowkeyvault.model.common.backup.KeyBackupListItem;
import com.github.nagyesta.lowkeyvault.model.common.backup.KeyBackupModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.*;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.JsonWebKeyImportRequest;
import com.github.nagyesta.lowkeyvault.service.key.KeyVaultFake;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.id.KeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.impl.KeyImportInput;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.Objects;

/**
 * Common logic of backup and restore controllers across the different API versions.
 */
@Slf4j
public abstract class CommonKeyBackupRestoreController
        extends BaseBackupRestoreController<KeyEntityId, VersionedKeyEntityId, ReadOnlyKeyVaultKeyEntity,
        KeyVaultKeyModel, DeletedKeyVaultKeyModel, KeyVaultKeyItemModel, DeletedKeyVaultKeyItemModel,
        KeyVaultFake, KeyPropertiesModel, KeyBackupListItem, KeyBackupList, KeyBackupModel> {

    protected CommonKeyBackupRestoreController(
            final VaultService vaultService,
            final KeyEntityToV72ModelConverter modelConverter,
            final KeyEntityToV72KeyItemModelConverter itemConverter,
            final KeyEntityToV72BackupConverter backupConverter) {
        super(vaultService, modelConverter, itemConverter, VaultFake::keyVaultFake, backupConverter::convert);
    }

    public ResponseEntity<KeyBackupModel> backup(
            @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            final URI baseUri,
            final String apiVersion) {
        log.info("Received request to {} backup key: {} using API version: {}",
                baseUri, keyName, apiVersion);
        return ResponseEntity.ok(backupEntity(entityId(baseUri, keyName)));
    }

    public ResponseEntity<KeyVaultKeyModel> restore(
            final URI baseUri,
            final String apiVersion,
            @Valid final KeyBackupModel keyBackupModel) {
        final var value = Objects.requireNonNull(keyBackupModel.getValue());
        log.info("Received request to {} restore key: {} using API version: {}",
                baseUri, value.getVersions().getFirst().getId(), apiVersion);
        return ResponseEntity.ok(restoreEntity(keyBackupModel));
    }

    @Override
    protected void restoreVersion(
            final KeyVaultFake vault,
            final VersionedKeyEntityId versionedEntityId,
            final KeyBackupListItem entityVersion) {
        final var keyMaterial = getKeyMaterial(entityVersion);
        final var attributes = entityVersion.getAttributes();
        vault.importKeyVersion(versionedEntityId, KeyImportInput.builder()
                .key(keyMaterial)
                .hsm(null)
                .managed(false)
                .enabled(attributes.isEnabled())
                .createdOn(attributes.getCreated())
                .updatedOn(attributes.getUpdated())
                .notBefore(attributes.getNotBefore())
                .expiresOn(attributes.getExpiry())
                .tags(entityVersion.getTags())
                .build());
    }

    private JsonWebKeyImportRequest getKeyMaterial(final KeyBackupListItem entityVersion) {
        return entityVersion.getKeyMaterial();
    }

    @Override
    protected KeyBackupList getBackupList() {
        return new KeyBackupList();
    }

    @Override
    protected KeyBackupModel getBackupModel() {
        return new KeyBackupModel();
    }

    @Override
    protected VersionedKeyEntityId versionedEntityId(
            final URI baseUri,
            final String name,
            final String version) {
        return new VersionedKeyEntityId(baseUri, name, version);
    }

    @Override
    protected KeyEntityId entityId(
            final URI baseUri,
            final String name) {
        return new KeyEntityId(baseUri, name);
    }
}
