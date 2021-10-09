package com.github.nagyesta.lowkeyvault.service.key.id;

import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.net.URI;
import java.util.UUID;

public class VersionedKeyEntityId extends KeyEntityId {

    public VersionedKeyEntityId(@NonNull final URI vault, @NonNull final String id) {
        this(vault, id, UUID.randomUUID().toString().replaceAll("-", ""));
    }

    public VersionedKeyEntityId(@NonNull final URI vault, @NonNull final String id, @NonNull final String version) {
        super(vault, id, version);
        Assert.notNull(version, "Version must not be null.");
    }
}
