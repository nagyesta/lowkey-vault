package com.github.nagyesta.lowkeyvault.service.secret.id;

import com.github.nagyesta.lowkeyvault.service.BaseEntityId;
import org.springframework.util.Assert;

import java.net.URI;

public class VersionedSecretEntityId
        extends SecretEntityId {

    public VersionedSecretEntityId(
            final URI vault,
            final String id) {
        this(vault, id, BaseEntityId.generateVersion());
    }

    public VersionedSecretEntityId(
            final URI vault,
            final String id,
            final String version) {
        super(vault, id, version);
        Assert.notNull(version, "Version must not be null.");
    }
}
