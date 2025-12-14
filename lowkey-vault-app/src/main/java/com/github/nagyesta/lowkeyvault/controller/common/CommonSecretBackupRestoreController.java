package com.github.nagyesta.lowkeyvault.controller.common;

import com.github.nagyesta.lowkeyvault.mapper.v7_2.secret.SecretEntityToV72BackupConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.secret.SecretEntityToV72ModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.secret.SecretEntityToV72SecretItemModelConverter;
import com.github.nagyesta.lowkeyvault.model.common.backup.SecretBackupList;
import com.github.nagyesta.lowkeyvault.model.common.backup.SecretBackupListItem;
import com.github.nagyesta.lowkeyvault.model.common.backup.SecretBackupModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.*;
import com.github.nagyesta.lowkeyvault.service.secret.ReadOnlyKeyVaultSecretEntity;
import com.github.nagyesta.lowkeyvault.service.secret.SecretVaultFake;
import com.github.nagyesta.lowkeyvault.service.secret.id.SecretEntityId;
import com.github.nagyesta.lowkeyvault.service.secret.id.VersionedSecretEntityId;
import com.github.nagyesta.lowkeyvault.service.secret.impl.SecretCreateInput;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.Objects;

@Slf4j
public abstract class CommonSecretBackupRestoreController
        extends BaseBackupRestoreController<SecretEntityId, VersionedSecretEntityId, ReadOnlyKeyVaultSecretEntity,
        KeyVaultSecretModel, DeletedKeyVaultSecretModel, KeyVaultSecretItemModel, DeletedKeyVaultSecretItemModel,
        SecretVaultFake, SecretPropertiesModel, SecretBackupListItem, SecretBackupList, SecretBackupModel> {

    protected CommonSecretBackupRestoreController(
            final VaultService vaultService,
            final SecretEntityToV72ModelConverter modelConverter,
            final SecretEntityToV72SecretItemModelConverter itemConverter,
            final SecretEntityToV72BackupConverter backupConverter) {
        super(vaultService, modelConverter, itemConverter, VaultFake::secretVaultFake, backupConverter::convert);
    }

    public ResponseEntity<SecretBackupModel> backup(
            @Valid @Pattern(regexp = NAME_PATTERN) final String secretName,
            final URI baseUri,
            final String apiVersion) {
        log.info("Received request to {} backup secret: {} using API version: {}",
                baseUri, secretName, apiVersion);
        return ResponseEntity.ok(backupEntity(entityId(baseUri, secretName)));
    }

    public ResponseEntity<KeyVaultSecretModel> restore(
            final URI baseUri,
            final String apiVersion,
            @Valid final SecretBackupModel secretBackupModel) {
        final var value = Objects.requireNonNull(secretBackupModel.getValue());
        log.info("Received request to {} restore secret: {} using API version: {}",
                baseUri, value.getVersions().getFirst().getId(), apiVersion);
        return ResponseEntity.ok(restoreEntity(secretBackupModel));
    }

    @Override
    protected void restoreVersion(
            final SecretVaultFake vault,
            final VersionedSecretEntityId versionedEntityId,
            final SecretBackupListItem entityVersion) {
        final var attributes = Objects.requireNonNullElse(entityVersion.getAttributes(), new SecretPropertiesModel());
        vault.createSecretVersion(versionedEntityId, SecretCreateInput.builder()
                .value(entityVersion.getValue())
                .contentType(entityVersion.getContentType())
                .tags(entityVersion.getTags())
                .createdOn(attributes.getCreated())
                .updatedOn(attributes.getUpdated())
                .notBefore(attributes.getNotBefore())
                .expiresOn(attributes.getExpiry())
                .managed(false)
                .enabled(attributes.isEnabled())
                .build());
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
    protected VersionedSecretEntityId versionedEntityId(
            final URI baseUri,
            final String name,
            final String version) {
        return new VersionedSecretEntityId(baseUri, name, version);
    }

    @Override
    protected SecretEntityId entityId(
            final URI baseUri,
            final String name) {
        return new SecretEntityId(baseUri, name);
    }
}
