package com.github.nagyesta.lowkeyvault.service;

import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@EqualsAndHashCode
public class BaseEntityId
        implements EntityId {

    private static final String URI_NO_VERSION_FORMAT = "%s/%s/%s";
    private static final String URI_VERSION_FORMAT = "%s/%s/%s/%s";
    private static final String EMPTY = "";
    private static final String DASH_REGEX = Pattern.quote("-");
    private final URI vault;
    private final String id;
    private final String version;
    private final String entityType;
    private final String entityPathSegment;
    private final String deletedEntityPathSegment;


    public BaseEntityId(
            @NonNull final URI vault,
            @NonNull final String id,
            final String version,
            @NonNull final String entityType) {
        this.vault = vault;
        this.id = id;
        this.version = version;
        this.entityType = entityType;
        this.entityPathSegment = entityType + "s";
        this.deletedEntityPathSegment = "deleted" + entityPathSegment;
    }

    protected static String generateVersion() {
        return UUID.randomUUID().toString().replaceAll(DASH_REGEX, "");
    }

    @Override
    public String entityType() {
        return entityType;
    }

    public String entityPathSegment() {
        return entityPathSegment;
    }

    public String deletedEntityPathSegment() {
        return deletedEntityPathSegment;
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
        return URI.create(URI_NO_VERSION_FORMAT
                .formatted(vaultUri, entityPathSegment(), id()));
    }

    @Override
    public URI asUri(@NonNull final URI vaultUri) {
        return URI.create(URI_VERSION_FORMAT
                .formatted(vaultUri, entityPathSegment(), id(), versionOrEmpty()));
    }

    private String versionOrEmpty() {
        return Optional.ofNullable(version()).orElse(EMPTY);
    }

    @Override
    public URI asRecoveryUri(@NonNull final URI vaultUri) {
        return URI.create(URI_NO_VERSION_FORMAT
                .formatted(vaultUri, deletedEntityPathSegment(), id()));
    }

    @Override
    public URI asUri(
            @NonNull final URI vaultUri,
            @NonNull final String query) {
        return URI.create(asUri(vaultUri).toString() + query);
    }
}
