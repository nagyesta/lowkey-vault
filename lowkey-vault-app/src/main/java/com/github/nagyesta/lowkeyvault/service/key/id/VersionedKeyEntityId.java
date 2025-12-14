package com.github.nagyesta.lowkeyvault.service.key.id;

import com.github.nagyesta.lowkeyvault.service.BaseEntityId;
import org.springframework.util.Assert;

import java.net.URI;
import java.util.Objects;

public class VersionedKeyEntityId extends KeyEntityId {

    public VersionedKeyEntityId(
            final URI vault,
            final String id) {
        this(vault, id, BaseEntityId.generateVersion());
    }

    public VersionedKeyEntityId(
            final URI vault,
            final String id,
            final String version) {
        super(vault, id, version);
        Assert.notNull(version, "Version must not be null.");
    }

    @Override
    public String version() {
        return Objects.requireNonNull(super.version());
    }
}
