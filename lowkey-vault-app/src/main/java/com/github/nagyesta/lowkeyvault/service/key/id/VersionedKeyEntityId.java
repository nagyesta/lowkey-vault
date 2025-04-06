package com.github.nagyesta.lowkeyvault.service.key.id;

import com.github.nagyesta.lowkeyvault.service.BaseEntityId;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.net.URI;

public class VersionedKeyEntityId
        extends KeyEntityId {

    public VersionedKeyEntityId(@NonNull final URI vault, @NonNull final String id) {
        this(vault, id, BaseEntityId.generateVersion());
    }

    public VersionedKeyEntityId(@NonNull final URI vault, @NonNull final String id, @NonNull final String version) {
        super(vault, id, version);
        Assert.notNull(version, "Version must not be null.");
    }
}
