package com.github.nagyesta.lowkeyvault.http;

import java.net.URI;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

public final class AuthorityOverrideFunction implements Function<URI, URI> {

    private static final String OPTIONAL_PORT_REGEX = "(:[0-9]+)?";
    private static final String IP_ADDRESS_REGEX =
            "(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])";
    private static final Pattern IPV4_REGEX = Pattern.compile("^" + IP_ADDRESS_REGEX + OPTIONAL_PORT_REGEX + "$");
    private static final Pattern ONLY_NUMBERS_AND_DOTS_REGEX = Pattern.compile("^[0-9\\\\.:]+$");
    private static final String HOST_REGEX =
            "(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])";
    private static final Pattern HOSTNAME_REGEX = Pattern.compile("^" + HOST_REGEX + OPTIONAL_PORT_REGEX + "$");

    private final String vaultAuthority;
    private final String containerAuthority;

    public AuthorityOverrideFunction(final String vaultAuthority, final String containerAuthority) {
        validate(vaultAuthority, "vaultAuthority");
        validate(containerAuthority, "containerAuthority");
        this.vaultAuthority = vaultAuthority;
        this.containerAuthority = containerAuthority;
    }

    private static void validate(final String hostOrIpv4AddressWithPort, final String paramName) {
        if (hostOrIpv4AddressWithPort == null) {
            throw new IllegalArgumentException(paramName + " must not be null!");
        }
        if (!(validIpv4Address(hostOrIpv4AddressWithPort) || validHostName(hostOrIpv4AddressWithPort))) {
            throw new IllegalArgumentException(paramName
                    + " must be an IPv4 address or a host name and an optional port number! "
                    + "Invalid value found: '" + hostOrIpv4AddressWithPort + "'.");
        }
    }

    private static boolean validIpv4Address(final String hostOrIpv4Address) {
        return hostOrIpv4Address.matches(IPV4_REGEX.pattern());
    }

    private static boolean validHostName(final String hostOrIpv4Address) {
        return !hostOrIpv4Address.matches(ONLY_NUMBERS_AND_DOTS_REGEX.pattern()) && hostOrIpv4Address.matches(HOSTNAME_REGEX.pattern());
    }

    @Override
    public URI apply(final URI originalUri) {
        return Optional.ofNullable(originalUri)
                .filter(uri -> uri.getAuthority().equals(vaultAuthority))
                .map(URI::toString)
                .map(uriAsString -> uriAsString.replaceFirst(Pattern.quote(vaultAuthority), containerAuthority))
                .map(URI::create)
                .orElse(originalUri);
    }
}
