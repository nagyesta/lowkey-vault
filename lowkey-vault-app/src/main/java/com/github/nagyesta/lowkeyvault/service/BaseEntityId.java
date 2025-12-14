package com.github.nagyesta.lowkeyvault.service;

import io.jsonwebtoken.lang.Assert;
import lombok.EqualsAndHashCode;
import org.jspecify.annotations.Nullable;

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
    @Nullable
    private final String version;
    private final String entityType;
    private final String entityPathSegment;
    private final String deletedEntityPathSegment;


    public BaseEntityId(
            final URI vault,
            final String id,
            @Nullable final String version,
            final String entityType) {
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
    public @Nullable String version() {
        return version;
    }

    @Override
    public String toString() {
        return asString();
    }

    @Override
    public URI asUriNoVersion(final URI vaultUri) {
        assertVaultUriIsNotNull(vaultUri);
        return URI.create(URI_NO_VERSION_FORMAT
                .formatted(vaultUri, entityPathSegment(), id()));
    }

    @Override
    public URI asUri(final URI vaultUri) {
        assertVaultUriIsNotNull(vaultUri);
        return URI.create(URI_VERSION_FORMAT
                .formatted(vaultUri, entityPathSegment(), id(), versionOrEmpty()));
    }

    @Override
    public URI asRecoveryUri(final URI vaultUri) {
        assertVaultUriIsNotNull(vaultUri);
        return URI.create(URI_NO_VERSION_FORMAT
                .formatted(vaultUri, deletedEntityPathSegment(), id()));
    }

    @Override
    public URI asUri(
            final URI vaultUri,
            final String query) {
        Assert.hasText(query, "Query cannot be empty.");
        assertVaultUriIsNotNull(vaultUri);
        return URI.create(asUri(vaultUri) + query);
    }

    private String versionOrEmpty() {
        return Optional.ofNullable(version()).orElse(EMPTY);
    }

    private void assertVaultUriIsNotNull(final URI vaultUri) {
        Assert.notNull(vaultUri, "Vault URI cannot be null.");
    }
}
