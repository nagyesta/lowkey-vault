package com.github.nagyesta.lowkeyvault.controller.common;

import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72BackupConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72KeyItemModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72ModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.key.KeyRotationPolicyToV73ModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.key.KeyRotationPolicyV73ModelToEntityConverter;
import com.github.nagyesta.lowkeyvault.model.common.backup.KeyBackupModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyVaultKeyModel;
import com.github.nagyesta.lowkeyvault.service.key.id.KeyEntityId;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@SuppressWarnings("java:S110")
public abstract class CommonPolicyAwareKeyBackupRestoreController extends CommonKeyBackupRestoreController {

    private final KeyRotationPolicyToV73ModelConverter rotationPolicyModelConverter;
    private final KeyRotationPolicyV73ModelToEntityConverter rotationPolicyEntityConverter;

    protected CommonPolicyAwareKeyBackupRestoreController(
            final VaultService vaultService,
            final KeyEntityToV72ModelConverter modelConverter,
            final KeyEntityToV72KeyItemModelConverter itemConverter,
            final KeyEntityToV72BackupConverter backupConverter,
            final KeyRotationPolicyToV73ModelConverter rotationPolicyModelConverter,
            final KeyRotationPolicyV73ModelToEntityConverter rotationPolicyEntityConverter) {
        super(vaultService, modelConverter, itemConverter, backupConverter);
        this.rotationPolicyModelConverter = rotationPolicyModelConverter;
        this.rotationPolicyEntityConverter = rotationPolicyEntityConverter;
    }

    @Override
    protected KeyBackupModel backupEntity(final KeyEntityId entityId) {
        final var keyBackupModel = super.backupEntity(entityId);
        final var value = Objects.requireNonNull(keyBackupModel.getValue());
        final var rotationPolicy = getVaultByUri(entityId.vault()).rotationPolicy(entityId);
        if (rotationPolicy != null) {
            value.setKeyRotationPolicy(rotationPolicyModelConverter.convert(rotationPolicy, entityId.vault()));
        }
        return keyBackupModel;
    }

    @Override
    protected KeyVaultKeyModel restoreEntity(final KeyBackupModel backupModel) {
        final var keyVaultKeyModel = super.restoreEntity(backupModel);
        final var baseUri = getSingleBaseUri(backupModel);
        final var entityName = getSingleEntityName(backupModel);
        final var keyEntityId = entityId(baseUri, entityName);
        final var vaultByUri = getVaultByUri(baseUri);
        final var keyRotationPolicy = Objects.requireNonNull(backupModel.getValue()).getKeyRotationPolicy();
        Optional.ofNullable(keyRotationPolicy)
                .map(r -> {
                    r.setKeyEntityId(keyEntityId);
                    return r;
                })
                .map(rotationPolicyEntityConverter::convert)
                .ifPresent(vaultByUri::setRotationPolicy);
        return keyVaultKeyModel;
    }

}
