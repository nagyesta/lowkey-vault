package com.github.nagyesta.lowkeyvault.controller.common;

import com.github.nagyesta.lowkeyvault.mapper.v7_2.secret.SecretEntityToV72ModelConverter;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.*;
import com.github.nagyesta.lowkeyvault.service.secret.ReadOnlyKeyVaultSecretEntity;
import com.github.nagyesta.lowkeyvault.service.secret.SecretVaultFake;
import com.github.nagyesta.lowkeyvault.service.secret.id.SecretEntityId;
import com.github.nagyesta.lowkeyvault.service.secret.id.VersionedSecretEntityId;
import com.github.nagyesta.lowkeyvault.service.secret.impl.KeyVaultSecretEntity;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.net.URI;

@Slf4j
public abstract class CommonSecretBackupRestoreController extends BaseBackupRestoreController<SecretEntityId, VersionedSecretEntityId,
        ReadOnlyKeyVaultSecretEntity, KeyVaultSecretModel, DeletedKeyVaultSecretModel, SecretPropertiesModel, SecretBackupListItem,
        SecretBackupList, SecretBackupModel, SecretEntityToV72BackupConverter, SecretEntityToV72ModelConverter, SecretVaultFake> {

    protected CommonSecretBackupRestoreController(
            @NonNull final SecretEntityToV72ModelConverter modelConverter,
            @NonNull final SecretEntityToV72BackupConverter backupConverter,
            @NonNull final VaultService vaultService) {
        super(modelConverter, backupConverter, vaultService, VaultFake::secretVaultFake);
    }

    public ResponseEntity<SecretBackupModel> backup(
            @Valid @Pattern(regexp = NAME_PATTERN) final String secretName,
            final URI baseUri) {
        log.info("Received request to {} backup secret: {} using API version: {}",
                baseUri.toString(), secretName, apiVersion());
        return ResponseEntity.ok(backupEntity(entityId(baseUri, secretName)));
    }

    public ResponseEntity<KeyVaultSecretModel> restore(
            final URI baseUri,
            @Valid final SecretBackupModel secretBackupModel) {
        log.info("Received request to {} restore secret: {} using API version: {}",
                baseUri.toString(), secretBackupModel.getValue().getVersions().get(0).getId(), apiVersion());
        return ResponseEntity.ok(restoreEntity(secretBackupModel));
    }

    @Override
    protected void restoreVersion(@NonNull final SecretVaultFake vault,
                                  @NonNull final VersionedSecretEntityId versionedEntityId,
                                  @NonNull final SecretBackupListItem entityVersion) {
        vault.createSecretVersion(versionedEntityId, entityVersion.getValue(), entityVersion.getContentType());
        final KeyVaultSecretEntity entity = vault.getEntities().getEntity(versionedEntityId, KeyVaultSecretEntity.class);
        updateCommonFields(entityVersion, entity);
    }

    @Override
    protected SecretBackupList getBackupList() {
        return new SecretBackupList();
    }

    @Override
    protected SecretBackupModel getBackupModel() {
        return new SecretBackupModel();
    }

    @Override
    protected VersionedSecretEntityId versionedEntityId(final URI baseUri, final String name, final String version) {
        return new VersionedSecretEntityId(baseUri, name, version);
    }

    @Override
    protected SecretEntityId entityId(final URI baseUri, final String name) {
        return new SecretEntityId(baseUri, name);
    }
}
