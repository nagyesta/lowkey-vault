package com.github.nagyesta.lowkeyvault.service.certificate.id;

import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.net.URI;
import java.util.UUID;

public class VersionedCertificateEntityId extends CertificateEntityId {

    public VersionedCertificateEntityId(@NonNull final URI vault, @NonNull final String id) {
        this(vault, id, UUID.randomUUID().toString().replaceAll("-", ""));
    }

    public VersionedCertificateEntityId(@NonNull final URI vault, @NonNull final String id, @NonNull final String version) {
        super(vault, id, version);
        Assert.notNull(version, "Version must not be null.");
    }
}
