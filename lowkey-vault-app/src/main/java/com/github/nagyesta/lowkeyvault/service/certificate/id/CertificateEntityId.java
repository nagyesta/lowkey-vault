package com.github.nagyesta.lowkeyvault.service.certificate.id;

import com.github.nagyesta.lowkeyvault.service.BaseEntityId;
import com.github.nagyesta.lowkeyvault.service.EntityId;
import org.jspecify.annotations.Nullable;

import java.net.URI;

public class CertificateEntityId extends BaseEntityId implements EntityId {

    public CertificateEntityId(
            final URI vault,
            final String id) {
        this(vault, id, null);
    }

    public CertificateEntityId(
            final URI vault,
            final String id,
            @Nullable final String version) {
        super(vault, id, version, "certificate");
    }

    public URI asPolicyUri(final URI vaultUri) {
        return URI.create(asUri(vaultUri) + "/policy");
    }

    public URI asPendingOperationUri(final URI vaultUri) {
        return URI.create(asUriNoVersion(vaultUri) + "/pending");
    }
}
