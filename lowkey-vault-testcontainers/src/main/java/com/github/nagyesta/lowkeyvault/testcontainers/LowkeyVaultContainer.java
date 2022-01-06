package com.github.nagyesta.lowkeyvault.testcontainers;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LowkeyVaultContainer extends GenericContainer<LowkeyVaultContainer> {

    static final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse("nagyesta/lowkey-vault");
    private static final String DUMMY_USERNAME = "DUMMY";
    private static final String DUMMY_PASSWORD = "DUMMY";
    private static final int CONTAINER_PORT = 8443;
    private static final Pattern NAME_PATTERN = Pattern.compile("^[0-9a-zA-Z-]+$");
    private static final String HTTPS = "https://";
    private static final String EMPTY = "";
    private static final String PORT_SEPARATOR = ":";
    private static final String LOCALHOST = "localhost";
    private static final String DOT = ".";

    /**
     * Creates a new instance.
     *
     * @param dockerImageName specified docker image name to run
     */
    public LowkeyVaultContainer(final DockerImageName dockerImageName) {
        this(dockerImageName, Collections.emptySet());
    }

    /**
     * Creates a new instance and sets vault names.
     *
     * @param dockerImageName specified docker image name to run
     * @param vaultNames      The names of the vaults we want to initialize at startup
     */
    public LowkeyVaultContainer(final DockerImageName dockerImageName, final Set<String> vaultNames) {
        super(dockerImageName);
        assertVaultNamesAreValid(vaultNames);

        dockerImageName.assertCompatibleWith(DEFAULT_IMAGE_NAME);
        addExposedPort(CONTAINER_PORT);

        final Set<String> names = Objects.requireNonNullElse(vaultNames, Collections.emptySet());
        if (!names.isEmpty()) {
            withEnv("LOWKEY_ARGS", "--LOWKEY_VAULT_NAMES=" + String.join(",", names));
        }
        waitingFor(Wait.forLogMessage("(?s).*Started LowkeyVaultApp.*$", 1));
    }

    /**
     * Returns the URL of the default vault.
     *
     * @return default vault base URL.
     */
    public String getDefaultVaultBaseUrl() {
        return HTTPS + getDefaultVaultAuthority();
    }


    /**
     * Returns the URL of the vault with a given name.
     *
     * @param vaultName the name of the vault
     * @return vault base URL.
     */
    public String getVaultBaseUrl(final String vaultName) {
        return HTTPS + getVaultAuthority(vaultName);
    }

    /**
     * Returns the authority of the URL belonging to the default vault.
     *
     * @return authority of the default vault base URL.
     */
    public String getDefaultVaultAuthority() {
        return LOCALHOST + PORT_SEPARATOR + CONTAINER_PORT;
    }

    /**
     * Returns the authority of the URL belonging to the vault with a given name.
     *
     * @param vaultName the name of the vault
     * @return authority of the given vault base URL.
     */
    public String getVaultAuthority(final String vaultName) {
        return Objects.requireNonNull(vaultName) + DOT + LOCALHOST + PORT_SEPARATOR + CONTAINER_PORT;
    }

    /**
     * Returns the authority of the endpoint we can use to access the container.
     *
     * @return endpoint authority.
     */
    public String getEndpointAuthority() {
        return getHost() + PORT_SEPARATOR + getMappedPort(CONTAINER_PORT);
    }

    /**
     * Returns the password to be used for authentication using com.azure.core.credential.BasicAuthenticationCredential.
     *
     * @return password
     */
    public String getPassword() {
        return DUMMY_PASSWORD;
    }

    /**
     * Returns the username to be used for authentication using com.azure.core.credential.BasicAuthenticationCredential.
     *
     * @return username
     */
    public String getUsername() {
        return DUMMY_USERNAME;
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
