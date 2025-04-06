package com.github.nagyesta.lowkeyvault.service.key.id;

import com.github.nagyesta.lowkeyvault.service.BaseEntityId;
import com.github.nagyesta.lowkeyvault.service.EntityId;
import lombok.NonNull;

import java.net.URI;

public class KeyEntityId
        extends BaseEntityId implements EntityId {

    private static final String URI_ROTATIONPOLICY_FORMAT = "%s/%s/%s/rotationpolicy";

    public KeyEntityId(final URI vault, final String id) {
        this(vault, id, null);
    }

    public KeyEntityId(
            @NonNull final URI vault,
            @NonNull final String id,
            final String version) {
        super(vault, id, version, "key");
    }

    public URI asRotationPolicyUri(@NonNull final URI vaultUri) {
        return URI.create(URI_ROTATIONPOLICY_FORMAT
                .formatted(vaultUri, entityPathSegment(), id()));
    }
}
