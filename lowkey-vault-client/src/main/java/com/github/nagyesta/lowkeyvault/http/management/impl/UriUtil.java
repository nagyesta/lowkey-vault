package com.github.nagyesta.lowkeyvault.http.management.impl;

import com.github.nagyesta.lowkeyvault.http.management.LowkeyVaultException;
import org.apache.http.client.utils.URIBuilder;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public final class UriUtil {

    private UriUtil() {
        throw new IllegalCallerException("Utility cannot be instantiated.");
    }

    public static URI uriBuilderForPath(
            final String baseUrl,
            final String path) {
        return uriBuilderForPath(baseUrl, path, Map.of());
    }

    public static URI uriBuilderForPath(
            final String baseUrl,
            final String path,
            @Nullable final Map<String, String> parameters) {
        try {
            Objects.requireNonNull(baseUrl, "BaseUrl cannot be null.");
            Objects.requireNonNull(path, "Path cannot be null.");
            final var builder = new URIBuilder(baseUrl).setPath(path);
            Objects.requireNonNullElse(parameters, Collections.<String, String>emptyMap()).forEach(builder::addParameter);
            return builder.build();
        } catch (final URISyntaxException e) {
            throw new LowkeyVaultException("Unable to parse baseUrl.", e);
        }
    }
}
