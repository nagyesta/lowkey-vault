package com.github.nagyesta.lowkeyvault.controller.common;

import com.github.nagyesta.lowkeyvault.mapper.common.registry.KeyConverterRegistry;
import com.github.nagyesta.lowkeyvault.model.common.backup.KeyBackupModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyVaultKeyModel;
import com.github.nagyesta.lowkeyvault.service.key.id.KeyEntityId;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

import java.util.Optional;

@Slf4j
public abstract class CommonPolicyAwareKeyBackupRestoreController extends CommonKeyBackupRestoreController {

    public CommonPolicyAwareKeyBackupRestoreController(
            @NonNull final KeyConverterRegistry registry, @NonNull final VaultService vaultService) {
        super(registry, vaultService);
    }

    @Override
    protected KeyBackupModel backupEntity(final KeyEntityId entityId) {
        final var keyBackupModel = super.backupEntity(entityId);
        final var value = keyBackupModel.getValue();
        final var rotationPolicy = getVaultByUri(entityId.vault()).rotationPolicy(entityId);
        value.setKeyRotationPolicy(registry().rotationPolicyModelConverter(apiVersion()).convert(rotationPolicy, entityId.vault()));
        return keyBackupModel;
    }

    @Override
    protected KeyVaultKeyModel restoreEntity(final KeyBackupModel backupModel) {
        final var keyVaultKeyModel = super.restoreEntity(backupModel);
        final var baseUri = getSingleBaseUri(backupModel);
        final var entityName = getSingleEntityName(backupModel);
        final var keyEntityId = entityId(baseUri, entityName);
        final var vaultByUri = getVaultByUri(baseUri);
        final var keyRotationPolicy = backupModel.getValue().getKeyRotationPolicy();
        Optional.ofNullable(keyRotationPolicy)
                .map(r -> {
                    r.setKeyEntityId(keyEntityId);
                    return r;
                })
                .map(r -> registry().rotationPolicyEntityConverter(apiVersion()).convert(r))
                .ifPresent(vaultByUri::setRotationPolicy);
        return keyVaultKeyModel;
    }

}
