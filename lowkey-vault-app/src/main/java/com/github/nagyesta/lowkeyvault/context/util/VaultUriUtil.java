package com.github.nagyesta.lowkeyvault.context.util;

import lombok.NonNull;

import java.net.URI;

public final class VaultUriUtil {

    private static final int DEFAULT_HTTPS_PORT = 443;
    private static final String HTTPS_SCHEME = "https://";

    private VaultUriUtil() {
        throw new IllegalCallerException("Utility cannot be instantiated.");
    }

    public static URI vaultUri(@NonNull final String hostname, final int optionalPort) {
        final StringBuilder builder = new StringBuilder(HTTPS_SCHEME).append(hostname);
        if (optionalPort != DEFAULT_HTTPS_PORT) {
            builder.append(":").append(optionalPort);
        }
        return URI.create(builder.toString());
    }
}
