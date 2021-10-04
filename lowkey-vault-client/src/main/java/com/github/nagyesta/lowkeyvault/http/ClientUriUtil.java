package com.github.nagyesta.lowkeyvault.http;

import java.net.URI;
import java.net.URL;
import java.util.regex.Pattern;

public final class ClientUriUtil {

    private static final String PLACEHOLDER = "___";
    private static final String PORT_SEPARATOR = ":";
    private static final int DEFAULT_PORT = -1;

    private ClientUriUtil() {
        throw new IllegalCallerException("Utility.");
    }

    public static URL revertPortHack(final URL url) {
        try {
            if (url == null) {
                return null;
            }
            final String asString = url.toString();
            final URL result;
            if (url.getHost().contains(PLACEHOLDER)) {
                result = URI.create(asString.replaceFirst(Pattern.quote(PLACEHOLDER), PORT_SEPARATOR)).toURL();
            } else {
                result = url;
            }
            return result;
        } catch (final Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    //Hacky work around for: https://github.com/Azure/azure-sdk-for-java/issues/24508
    public static String hackPort(final String uriAsString) {
        try {
            if (uriAsString == null) {
                return null;
            }
            final URI uri = URI.create(uriAsString);
            final String result;
            if (uri.getPort() != DEFAULT_PORT) {
                result = uri.toString().replaceFirst(Pattern.quote(uri.getHost() + PORT_SEPARATOR), uri.getHost() + PLACEHOLDER);
            } else {
                result = uriAsString;
            }
            return result;
        } catch (final Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
