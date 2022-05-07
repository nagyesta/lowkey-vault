package com.github.nagyesta.lowkeyvault.controller.common;

import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72BackupConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72ModelConverter;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.*;
import com.github.nagyesta.lowkeyvault.service.key.KeyVaultFake;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.id.KeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.impl.KeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.net.URI;
import java.util.Collections;
import java.util.Objects;

@Slf4j
public abstract class CommonKeyBackupRestoreController extends BaseBackupRestoreController<KeyEntityId, VersionedKeyEntityId,
        ReadOnlyKeyVaultKeyEntity, KeyVaultKeyModel, DeletedKeyVaultKeyModel, KeyPropertiesModel, KeyBackupListItem, KeyBackupList,
        KeyBackupModel, KeyEntityToV72BackupConverter, KeyEntityToV72ModelConverter, KeyVaultFake> {

    protected CommonKeyBackupRestoreController(
            @NonNull final KeyEntityToV72ModelConverter modelConverter,
            @NonNull final KeyEntityToV72BackupConverter backupConverter,
            @NonNull final VaultService vaultService) {
        super(modelConverter, backupConverter, vaultService, VaultFake::keyVaultFake);
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
                baseUri.toString(), keyBackupModel.getValue().get(0).getId(), apiVersion());
        return ResponseEntity.ok(restoreEntity(keyBackupModel));
    }

    @Override
    protected void restoreVersion(@NonNull final KeyVaultFake vault,
                                  @NonNull final VersionedKeyEntityId versionedEntityId,
                                  @NonNull final KeyBackupListItem entityVersion) {
        vault.importKeyVersion(versionedEntityId, entityVersion.getKeyMaterial());
        final KeyVaultKeyEntity<?, ?> entity = vault.getEntities().getEntity(versionedEntityId, KeyVaultKeyEntity.class);
        entity.setOperations(Objects.requireNonNullElse(entityVersion.getKeyMaterial().getKeyOps(), Collections.emptyList()));
        updateCommonFields(entityVersion, entity);
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
    protected VersionedKeyEntityId versionedEntityId(final URI baseUri, final String name, final String version) {
        return new VersionedKeyEntityId(baseUri, name, version);
    }

    @Override
    protected KeyEntityId entityId(final URI baseUri, final String name) {
        return new KeyEntityId(baseUri, name);
    }
}
