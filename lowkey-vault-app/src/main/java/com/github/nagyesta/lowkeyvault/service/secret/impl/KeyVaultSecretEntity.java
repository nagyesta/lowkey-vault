package com.github.nagyesta.lowkeyvault.service.secret.impl;

import com.github.nagyesta.lowkeyvault.service.common.impl.KeyVaultBaseEntity;
import com.github.nagyesta.lowkeyvault.service.secret.ReadOnlyKeyVaultSecretEntity;
import com.github.nagyesta.lowkeyvault.service.secret.id.VersionedSecretEntityId;
import com.github.nagyesta.lowkeyvault.service.vault.VaultStub;
import lombok.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.net.URI;

public class KeyVaultSecretEntity extends KeyVaultBaseEntity<VersionedSecretEntityId> implements ReadOnlyKeyVaultSecretEntity {

    private final String value;
    private final String contentType;
    private final VersionedSecretEntityId id;
    private final URI uri;

    public KeyVaultSecretEntity(@NonNull final VersionedSecretEntityId id,
                                @org.springframework.lang.NonNull final VaultStub vault,
                                @org.springframework.lang.NonNull final String value,
                                @Nullable final String contentType) {
        super(vault);
        Assert.hasText(value, "Value must not be null or blank.");
        this.id = id;
        this.uri = id.asUri();
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

    @Override
    public URI getUri() {
        return uri;
    }

}
