package com.github.nagyesta.lowkeyvault.controller.common;

import com.github.nagyesta.lowkeyvault.mapper.v7_2.secret.SecretEntityToV72ModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.secret.SecretEntityToV72SecretItemModelConverter;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.*;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.request.CreateSecretRequest;
import com.github.nagyesta.lowkeyvault.service.secret.ReadOnlyKeyVaultSecretEntity;
import com.github.nagyesta.lowkeyvault.service.secret.SecretVaultFake;
import com.github.nagyesta.lowkeyvault.service.secret.id.SecretEntityId;
import com.github.nagyesta.lowkeyvault.service.secret.id.VersionedSecretEntityId;
import com.github.nagyesta.lowkeyvault.service.secret.impl.SecretCreateInput;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
public abstract class BaseSecretController
        extends GenericEntityController<SecretEntityId, VersionedSecretEntityId, ReadOnlyKeyVaultSecretEntity, KeyVaultSecretModel,
        DeletedKeyVaultSecretModel, KeyVaultSecretItemModel, DeletedKeyVaultSecretItemModel, SecretVaultFake> {

    protected BaseSecretController(
            final VaultService vaultService,
            final SecretEntityToV72ModelConverter modelConverter,
            final SecretEntityToV72SecretItemModelConverter itemConverter) {
        super(vaultService, modelConverter, itemConverter, VaultFake::secretVaultFake);
    }

    protected VersionedSecretEntityId createSecretWithAttributes(
            final SecretVaultFake secretVaultFake,
            final String secretName,
            final CreateSecretRequest request) {
        final var properties = Objects.requireNonNullElse(request.getProperties(), new SecretPropertiesModel());
        return secretVaultFake.createSecretVersion(secretName, SecretCreateInput.builder()
                .value(request.getValue())
                .contentType(request.getContentType())
                .tags(request.getTags())
                .expiresOn(properties.getExpiry())
                .notBefore(properties.getNotBefore())
                .enabled(properties.isEnabled())
                .managed(false)
                .build());
    }
}
