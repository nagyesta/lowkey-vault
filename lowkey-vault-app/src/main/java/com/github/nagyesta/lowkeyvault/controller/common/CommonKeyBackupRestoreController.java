package com.github.nagyesta.lowkeyvault.controller.common;

import com.github.nagyesta.lowkeyvault.mapper.common.BackupConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72ModelConverter;
import com.github.nagyesta.lowkeyvault.model.v7_2.common.BaseBackupListItem;
import com.github.nagyesta.lowkeyvault.model.v7_2.common.BaseBackupModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.BackupListContainer;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.DeletedKeyVaultKeyModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyPropertiesModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyVaultKeyModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.JsonWebKeyImportRequest;
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

/**
 * Common logic of backup and restore controllers across the different API versions.
 *
 * @param <BLI> The type of the list item representing one entity version in the backup model.
 * @param <BL>  The wrapper type of the list in the backup model.
 * @param <B>   The type of the backup model.
 * @param <BC>  The converter, converting entities to list items of the backup models.
 */
@Slf4j
public abstract class CommonKeyBackupRestoreController<BLI extends BaseBackupListItem<KeyPropertiesModel>,
        BL extends BackupListContainer<BLI>, B extends BaseBackupModel<KeyPropertiesModel, BLI, BL>,
        BC extends BackupConverter<VersionedKeyEntityId, ReadOnlyKeyVaultKeyEntity, KeyPropertiesModel, BLI>>
        extends BaseBackupRestoreController<KeyEntityId, VersionedKeyEntityId,
        ReadOnlyKeyVaultKeyEntity, KeyVaultKeyModel, DeletedKeyVaultKeyModel, KeyPropertiesModel, BLI, BL,
        B, BC, KeyEntityToV72ModelConverter, KeyVaultFake> {

    protected CommonKeyBackupRestoreController(
            @NonNull final KeyEntityToV72ModelConverter modelConverter,
            @NonNull final BC backupConverter,
            @NonNull final VaultService vaultService) {
        super(modelConverter, backupConverter, vaultService, VaultFake::keyVaultFake);
    }

    public ResponseEntity<B> backup(
            @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            final URI baseUri) {
        log.info("Received request to {} backup key: {} using API version: {}",
                baseUri.toString(), keyName, apiVersion());
        return ResponseEntity.ok(backupEntity(entityId(baseUri, keyName)));
    }

    public ResponseEntity<KeyVaultKeyModel> restore(
            final URI baseUri,
            @Valid final B keyBackupModel) {
        log.info("Received request to {} restore key: {} using API version: {}",
                baseUri.toString(), keyBackupModel.getValue().getVersions().get(0).getId(), apiVersion());
        return ResponseEntity.ok(restoreEntity(keyBackupModel));
    }

    @Override
    protected void restoreVersion(@NonNull final KeyVaultFake vault,
                                  @NonNull final VersionedKeyEntityId versionedEntityId,
                                  @NonNull final BLI entityVersion) {
        vault.importKeyVersion(versionedEntityId, getKeyMaterial(entityVersion));
        final KeyVaultKeyEntity<?, ?> entity = vault.getEntities().getEntity(versionedEntityId, KeyVaultKeyEntity.class);
        entity.setOperations(Objects.requireNonNullElse(getKeyMaterial(entityVersion).getKeyOps(), Collections.emptyList()));
        updateCommonFields(entityVersion, entity);
    }

    @Override
    protected VersionedKeyEntityId versionedEntityId(final URI baseUri, final String name, final String version) {
        return new VersionedKeyEntityId(baseUri, name, version);
    }

    @Override
    protected KeyEntityId entityId(final URI baseUri, final String name) {
        return new KeyEntityId(baseUri, name);
    }

    protected abstract JsonWebKeyImportRequest getKeyMaterial(BLI entityVersion);
}
