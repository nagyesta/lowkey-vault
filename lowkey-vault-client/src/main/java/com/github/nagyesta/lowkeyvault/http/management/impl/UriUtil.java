package com.github.nagyesta.lowkeyvault.http.management.impl;

import com.github.nagyesta.lowkeyvault.http.management.LowkeyVaultException;
import lombok.NonNull;
import org.apache.http.client.utils.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public final class UriUtil {

    private UriUtil() {
        throw new IllegalCallerException("Utility cannot be instantiated.");
    }

    public static URI uriBuilderForPath(final String baseUrl, final String path) {
        return uriBuilderForPath(baseUrl, path, Map.of());
    }

    public static URI uriBuilderForPath(@NonNull final String baseUrl, @NonNull final String path, final Map<String, String> parameters) {
        try {
            final URIBuilder builder = new URIBuilder(baseUrl).setPath(path);
            Objects.requireNonNullElse(parameters, Collections.<String, String>emptyMap()).forEach(builder::addParameter);
            return builder.build();
        } catch (final URISyntaxException e) {
            throw new LowkeyVaultException("Unable to parse baseUrl.", e);
        }
    }
}
