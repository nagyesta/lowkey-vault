package com.github.nagyesta.lowkeyvault.controller.common;

import com.github.nagyesta.lowkeyvault.mapper.common.registry.KeyConverterRegistry;
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
import org.springframework.lang.NonNull;

import java.net.URI;

/**
 * Common logic of backup and restore controllers across the different API versions.
 */
@Slf4j
public abstract class CommonKeyBackupRestoreController
        extends BaseBackupRestoreController<KeyEntityId, VersionedKeyEntityId, ReadOnlyKeyVaultKeyEntity,
        KeyVaultKeyModel, DeletedKeyVaultKeyModel, KeyVaultKeyItemModel, DeletedKeyVaultKeyItemModel,
        KeyVaultFake, KeyPropertiesModel, KeyBackupListItem, KeyBackupList, KeyBackupModel, KeyConverterRegistry> {

    protected CommonKeyBackupRestoreController(
            @NonNull final KeyConverterRegistry registry,
            @NonNull final VaultService vaultService) {
        super(registry, vaultService, VaultFake::keyVaultFake);
    }

    public ResponseEntity<KeyBackupModel> backup(
            @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            final URI baseUri) {
        log.info("Received request to {} backup key: {} using API version: {}",
                baseUri.toString(), keyName, apiVersion());
        return ResponseEntity.ok(backupEntity(entityId(baseUri, keyName)));
    }

    public ResponseEntity<KeyVaultKeyModel> restore(
            final URI baseUri,
            @Valid final KeyBackupModel keyBackupModel) {
        log.info("Received request to {} restore key: {} using API version: {}",
                baseUri.toString(), keyBackupModel.getValue().getVersions().get(0).getId(), apiVersion());
        return ResponseEntity.ok(restoreEntity(keyBackupModel));
    }

    @Override
    protected void restoreVersion(
            @NonNull final KeyVaultFake vault,
            @NonNull final VersionedKeyEntityId versionedEntityId,
            @NonNull final KeyBackupListItem entityVersion) {
        final var keyMaterial = getKeyMaterial(entityVersion);
        final var attributes = entityVersion.getAttributes();
        vault.importKeyVersion(versionedEntityId, KeyImportInput.builder()
                .key(keyMaterial)
                .hsm(null)
                .managed(false)
                .enabled(attributes.isEnabled())
                .createdOn(attributes.getCreatedOn())
                .updatedOn(attributes.getUpdatedOn())
                .notBefore(attributes.getNotBefore())
                .expiresOn(attributes.getExpiresOn())
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
}
