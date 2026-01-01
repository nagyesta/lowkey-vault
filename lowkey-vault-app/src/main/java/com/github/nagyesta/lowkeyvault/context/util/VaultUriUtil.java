package com.github.nagyesta.lowkeyvault.context.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.net.URI;

public final class VaultUriUtil {

    private static final int DEFAULT_HTTPS_PORT = 443;
    private static final String HTTPS_SCHEME = "https://";
    private static final String HOST_NAME = "[0-9a-z.\\-_]+";
    private static final String PORT = "\\d+";
    private static final String COLON = ":";
    private static final String PORT_PLACEHOLDER = "<port>";
    private static final String AUTHORITY_REGEX = "^" + HOST_NAME + "(" + COLON + PORT + "|" + COLON + PORT_PLACEHOLDER + ")?$";

    private VaultUriUtil() {
        throw new IllegalCallerException("Utility cannot be instantiated.");
    }

    public static URI vaultUri(
            final String hostname,
            final int optionalPort) {
        final var builder = new StringBuilder(HTTPS_SCHEME).append(hostname);
        if (optionalPort != DEFAULT_HTTPS_PORT) {
            builder.append(COLON).append(optionalPort);
        }
        final var result = URI.create(builder.toString());
        if (result.getHost() == null) {
            throw new IllegalArgumentException("URI couldn't be parsed: " + builder);
        }
        return result;
    }

    public static URI aliasUri(
            final String vaultAuthority,
            final int serverPort) {
        if (!vaultAuthority.matches(AUTHORITY_REGEX)) {
            throw new IllegalArgumentException("Alias authority must match: " + AUTHORITY_REGEX);
        }
        var authority = Strings.CS.replace(vaultAuthority, PORT_PLACEHOLDER, Integer.toString(serverPort));
        if (!authority.contains(COLON)) {
            authority = authority + COLON + DEFAULT_HTTPS_PORT;
        }
        final var hostname = StringUtils.substringBefore(authority, COLON);
        final var port = Integer.parseInt(StringUtils.substringAfter(authority, COLON));
        return VaultUriUtil.vaultUri(hostname, port);
    }

    public static URI replacePortWith(
            final URI uri,
            final int port) {
        return VaultUriUtil.vaultUri(uri.getHost(), port);
    }
}
