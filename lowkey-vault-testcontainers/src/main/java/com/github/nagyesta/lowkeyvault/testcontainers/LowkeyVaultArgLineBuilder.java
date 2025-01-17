package com.github.nagyesta.lowkeyvault.testcontainers;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LowkeyVaultArgLineBuilder {
    private static final String NO_AUTO_REGISTRATION_VALUE = "-";
    private static final Set<String> NO_AUTO_REGISTRATION = Set.of(NO_AUTO_REGISTRATION_VALUE);
    private static final String EMPTY = "";
    private static final Pattern NAME_PATTERN = Pattern.compile("^[0-9a-zA-Z-]+$");

    private final List<String> args;

    public LowkeyVaultArgLineBuilder() {
        args = new ArrayList<>();
        args.add("--LOWKEY_VAULT_RELAXED_PORTS=true");
    }

    public LowkeyVaultArgLineBuilder vaultNames(final Set<String> vaultNames) {
        if (!NO_AUTO_REGISTRATION.equals(vaultNames)) {
            assertVaultNamesAreValid(vaultNames);
        }
        if (!vaultNames.isEmpty()) {
            args.add("--LOWKEY_VAULT_NAMES=" + String.join(",", vaultNames));
        }
        return this;
    }

    public LowkeyVaultArgLineBuilder logicalHost(final String logicalHost) {
        if (logicalHost != null) {
            args.add("--LOWKEY_IMPORT_TEMPLATE_HOST=" + logicalHost);
        }
        return this;
    }

    public LowkeyVaultArgLineBuilder logicalPort(final Integer logicalPort) {
        if (logicalPort != null) {
            args.add("--LOWKEY_IMPORT_TEMPLATE_PORT=" + logicalPort);
        }
        return this;
    }

    public LowkeyVaultArgLineBuilder debug(final boolean debug) {
        if (debug) {
            args.add("--LOWKEY_DEBUG_REQUEST_LOG=true");
        }
        return this;
    }

    public LowkeyVaultArgLineBuilder importFile(final File file) {
        if (file != null) {
            args.add("--LOWKEY_IMPORT_LOCATION=/import/vaults.json");
        }
        return this;
    }

    public LowkeyVaultArgLineBuilder customSSLCertificate(final File file, final String password, final StoreType type) {
        if (file != null) {
            args.add("--server.ssl.key-store=/config/cert.store");
            args.add("--server.ssl.key-store-type=" + Optional.ofNullable(type).orElse(StoreType.PKCS12).name());
            args.add("--server.ssl.key-store-password=" + Optional.ofNullable(password).orElse(""));
        }
        return this;
    }

    public LowkeyVaultArgLineBuilder aliases(final Map<String, Set<String>> aliases) {
        if (aliases != null && !aliases.isEmpty()) {
            final String aliasMappings = new TreeMap<>(aliases).entrySet()
                    .stream()
                    .flatMap(e -> new TreeSet<>(e.getValue()).stream().map(alias -> e.getKey() + "=" + alias))
                    .collect(Collectors.joining(","));
            args.add("--LOWKEY_VAULT_ALIASES=" + aliasMappings);
        }
        return this;
    }

    public LowkeyVaultArgLineBuilder additionalArgs(final List<String> additionalArgs) {
        if (additionalArgs != null && !additionalArgs.isEmpty()) {
            args.addAll(additionalArgs);
        }
        return this;
    }

    public List<String> build() {
        return Collections.unmodifiableList(args);
    }

    private void assertVaultNamesAreValid(final Set<String> vaultNames) {
        if (vaultNames == null) {
            throw new IllegalArgumentException("VaultNames must not be null.");
        }
        final Collection<String> invalid = vaultNames.stream()
                .filter(s -> !NAME_PATTERN.matcher(Objects.requireNonNullElse(s, EMPTY)).matches())
                .collect(Collectors.toList());
        if (!invalid.isEmpty()) {
            throw new IllegalArgumentException("VaultNames contains invalid values: " + invalid);
        }
    }
}
