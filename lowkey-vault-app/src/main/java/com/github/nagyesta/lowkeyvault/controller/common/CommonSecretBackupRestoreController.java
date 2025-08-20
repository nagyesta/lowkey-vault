package com.github.nagyesta.lowkeyvault.controller.common;

import com.github.nagyesta.lowkeyvault.mapper.common.registry.SecretConverterRegistry;
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
import org.springframework.lang.NonNull;

import java.net.URI;
import java.util.Objects;

@Slf4j
public abstract class CommonSecretBackupRestoreController
        extends BaseBackupRestoreController<SecretEntityId, VersionedSecretEntityId, ReadOnlyKeyVaultSecretEntity,
        KeyVaultSecretModel, DeletedKeyVaultSecretModel, KeyVaultSecretItemModel, DeletedKeyVaultSecretItemModel,
        SecretVaultFake, SecretPropertiesModel, SecretBackupListItem, SecretBackupList, SecretBackupModel,
        SecretConverterRegistry> {

    protected CommonSecretBackupRestoreController(
            @NonNull final SecretConverterRegistry registry,
            @NonNull final VaultService vaultService) {
        super(registry, vaultService, VaultFake::secretVaultFake);
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
                baseUri.toString(), secretBackupModel.getValue().getVersions().getFirst().getId(), apiVersion());
        return ResponseEntity.ok(restoreEntity(secretBackupModel));
    }

    @Override
    protected void restoreVersion(
            @NonNull final SecretVaultFake vault,
            @NonNull final VersionedSecretEntityId versionedEntityId,
            @NonNull final SecretBackupListItem entityVersion) {
        final var attributes = Objects.requireNonNullElse(entityVersion.getAttributes(), new SecretPropertiesModel());
        vault.createSecretVersion(versionedEntityId, SecretCreateInput.builder()
                .value(entityVersion.getValue())
                .contentType(entityVersion.getContentType())
                .tags(entityVersion.getTags())
                .createdOn(attributes.getCreatedOn())
                .updatedOn(attributes.getUpdatedOn())
                .notBefore(attributes.getNotBefore())
                .expiresOn(attributes.getExpiresOn())
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

}
