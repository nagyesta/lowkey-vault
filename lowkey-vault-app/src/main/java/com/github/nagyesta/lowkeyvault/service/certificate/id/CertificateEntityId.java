package com.github.nagyesta.lowkeyvault.service.certificate.id;

import com.github.nagyesta.lowkeyvault.service.EntityId;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.net.URI;
import java.util.Optional;

@EqualsAndHashCode
public class CertificateEntityId implements EntityId {

    private final URI vault;
    private final String id;
    private final String version;

    public CertificateEntityId(final URI vault, final String id) {
        this(vault, id, null);
    }

    public CertificateEntityId(@NonNull final URI vault, @NonNull final String id, final String version) {
        this.vault = vault;
        this.id = id;
        this.version = version;
    }

    @Override
    public String entityType() {
        return "certificate";
    }

    @Override
    public URI vault() {
        return vault;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String version() {
        return version;
    }

    @Override
    public String toString() {
        return asString();
    }

    @Override
    public URI asUriNoVersion(@NonNull final URI vaultUri) {
        return URI.create(vaultUri + "/certificates/" + id());
    }

    @Override
    public URI asUri(@NonNull final URI vaultUri) {
        return URI.create(vaultUri + "/certificates/" + id() + "/" + Optional.ofNullable(version()).orElse(""));
    }

    @Override
    public URI asRecoveryUri(@NonNull final URI vaultUri) {
        return URI.create(vaultUri + "/deletedcertificates/" + id());
    }

    @Override
    public URI asUri(@NonNull final URI vaultUri, @NonNull final String query) {
        return URI.create(asUri(vaultUri).toString() + query);
    }

    public URI asPolicyUri(@NonNull final URI vaultUri) {
        return URI.create(asUri(vaultUri).toString() + "/policy");
    }

    public URI asPendingOperationUri(@NonNull final URI vaultUri) {
        return URI.create(asUriNoVersion(vaultUri).toString() + "/pending");
    }
}
