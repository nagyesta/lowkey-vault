package com.github.nagyesta.lowkeyvault.service.secret.impl;

import com.github.nagyesta.lowkeyvault.service.common.impl.KeyVaultBaseEntity;
import com.github.nagyesta.lowkeyvault.service.secret.ReadOnlyKeyVaultSecretEntity;
import com.github.nagyesta.lowkeyvault.service.secret.id.VersionedSecretEntityId;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

public class KeyVaultSecretEntity
        extends KeyVaultBaseEntity<VersionedSecretEntityId>
        implements ReadOnlyKeyVaultSecretEntity {

    @Setter
    private String value;
    private final String contentType;
    private final VersionedSecretEntityId id;

    public KeyVaultSecretEntity(
            @NonNull final VersionedSecretEntityId id,
            @org.springframework.lang.NonNull final VaultFake vault,
            @org.springframework.lang.NonNull final String value,
            @Nullable final String contentType) {
        super(vault);
        Assert.hasText(value, "Value must not be null or blank.");
        this.id = id;
        this.value = value;
        this.contentType = contentType;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public VersionedSecretEntityId getId() {
        return id;
    }

}
