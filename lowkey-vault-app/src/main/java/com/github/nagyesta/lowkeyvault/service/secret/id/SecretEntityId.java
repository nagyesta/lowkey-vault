package com.github.nagyesta.lowkeyvault.service.secret.id;

import com.github.nagyesta.lowkeyvault.service.BaseEntityId;
import com.github.nagyesta.lowkeyvault.service.EntityId;
import org.jspecify.annotations.Nullable;

import java.net.URI;

public class SecretEntityId
        extends BaseEntityId implements EntityId {

    public SecretEntityId(
            final URI vault,
            final String id) {
        this(vault, id, null);
    }

    public SecretEntityId(
            final URI vault,
            final String id,
            @Nullable final String version) {
        super(vault, id, version, "secret");
    }
}
