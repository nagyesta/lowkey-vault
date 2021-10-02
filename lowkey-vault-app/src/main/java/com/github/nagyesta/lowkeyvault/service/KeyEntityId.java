package com.github.nagyesta.lowkeyvault.service;

import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.net.URI;
import java.util.Optional;

@EqualsAndHashCode
public class KeyEntityId implements EntityId {

    private final URI vault;
    private final String id;
    private final String version;

    public KeyEntityId(@NonNull final URI vault, @NonNull final String id, final String version) {
        this.vault = vault;
        this.id = id;
        this.version = version;
    }

    @Override
    public String entityType() {
        return "key";
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
    public URI asUri() {
        return URI.create(vault + "/keys/" + id() + "/" + Optional.ofNullable(version()).orElse(""));
    }

    @Override
    public URI asUri(@NonNull final String query) {
        return URI.create(asUri().toString() + query);
    }
}
