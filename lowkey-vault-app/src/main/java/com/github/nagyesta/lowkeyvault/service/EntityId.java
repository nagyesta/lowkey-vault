package com.github.nagyesta.lowkeyvault.service;

import java.net.URI;
import java.util.Optional;

public interface EntityId {

    String entityType();

    URI vault();

    String id();

    String version();

    default String asString() {
        return entityType() + ":" + vault() + "/" + id() + "/" + Optional.ofNullable(version()).orElse("--");
    }

    URI asUriNoVersion(URI vaultUri);

    URI asUri(URI vaultUri);

    URI asRecoveryUri(URI vaultUri);

    URI asUri(URI vaultUri, String query);
}
