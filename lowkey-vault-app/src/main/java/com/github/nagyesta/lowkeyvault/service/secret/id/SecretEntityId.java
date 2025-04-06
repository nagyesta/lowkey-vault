package com.github.nagyesta.lowkeyvault.service.secret.id;

import com.github.nagyesta.lowkeyvault.service.BaseEntityId;
import com.github.nagyesta.lowkeyvault.service.EntityId;
import lombok.NonNull;

import java.net.URI;

public class SecretEntityId
        extends BaseEntityId implements EntityId {

    public SecretEntityId(
            final URI vault,
            final String id) {
        this(vault, id, null);
    }

    public SecretEntityId(
            @NonNull final URI vault,
            @NonNull final String id,
            final String version) {
        super(vault, id, version, "secret");
    }
}
