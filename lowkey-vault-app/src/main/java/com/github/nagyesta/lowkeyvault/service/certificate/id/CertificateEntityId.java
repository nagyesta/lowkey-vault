package com.github.nagyesta.lowkeyvault.service.certificate.id;

import com.github.nagyesta.lowkeyvault.service.BaseEntityId;
import com.github.nagyesta.lowkeyvault.service.EntityId;
import lombok.NonNull;

import java.net.URI;

public class CertificateEntityId
        extends BaseEntityId implements EntityId {

    public CertificateEntityId(
            final URI vault,
            final String id) {
        this(vault, id, null);
    }

    public CertificateEntityId(
            @NonNull final URI vault,
            @NonNull final String id,
            final String version) {
        super(vault, id, version, "certificate");
    }

    public URI asPolicyUri(@NonNull final URI vaultUri) {
        return URI.create(asUri(vaultUri).toString() + "/policy");
    }

    public URI asPendingOperationUri(@NonNull final URI vaultUri) {
        return URI.create(asUriNoVersion(vaultUri).toString() + "/pending");
    }
}
