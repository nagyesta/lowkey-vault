package com.github.nagyesta.lowkeyvault.service.secret.id;

import com.github.nagyesta.lowkeyvault.service.EntityId;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.net.URI;
import java.util.Optional;

@EqualsAndHashCode
public class SecretEntityId implements EntityId {

    private final URI vault;
    private final String id;
    private final String version;

    public SecretEntityId(final URI vault, final String id) {
        this(vault, id, null);
    }

    public SecretEntityId(@NonNull final URI vault, @NonNull final String id, final String version) {
        this.vault = vault;
        this.id = id;
        this.version = version;
    }

    @Override
    public String entityType() {
        return "secret";
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
    public URI asUriNoVersion() {
        return URI.create(vault + "/secrets/" + id());
    }

    @Override
    public URI asUri() {
        return URI.create(vault + "/secrets/" + id() + "/" + Optional.ofNullable(version()).orElse(""));
    }

    @Override
    public URI asRecoveryUri() {
        return URI.create(vault + "/deletedsecrets/" + id());
    }

    @Override
    public URI asUri(@NonNull final String query) {
        return URI.create(asUri().toString() + query);
    }
}
