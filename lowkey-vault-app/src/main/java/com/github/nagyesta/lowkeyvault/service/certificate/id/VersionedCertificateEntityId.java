package com.github.nagyesta.lowkeyvault.service.certificate.id;

import com.github.nagyesta.lowkeyvault.service.BaseEntityId;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.net.URI;

public class VersionedCertificateEntityId extends CertificateEntityId {

    public VersionedCertificateEntityId(@NonNull final URI vault, @NonNull final String id) {
        this(vault, id, BaseEntityId.generateVersion());
    }

    public VersionedCertificateEntityId(@NonNull final URI vault, @NonNull final String id, @NonNull final String version) {
        super(vault, id, version);
        Assert.notNull(version, "Version must not be null.");
    }
}
