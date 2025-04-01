package com.github.nagyesta.lowkeyvault.service.secret.id;

import com.github.nagyesta.lowkeyvault.service.BaseEntityId;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.net.URI;

public class VersionedSecretEntityId
        extends SecretEntityId {

    public VersionedSecretEntityId(
            @NonNull final URI vault,
            @NonNull final String id) {
        this(vault, id, BaseEntityId.generateVersion());
    }

    public VersionedSecretEntityId(
            @NonNull final URI vault,
            @NonNull final String id,
            @NonNull final String version) {
        super(vault, id, version);
        Assert.notNull(version, "Version must not be null.");
    }
}
