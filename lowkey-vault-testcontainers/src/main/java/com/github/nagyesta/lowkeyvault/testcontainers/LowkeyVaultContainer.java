package com.github.nagyesta.lowkeyvault.testcontainers;

import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.github.nagyesta.lowkeyvault.testcontainers.LowkeyVaultContainerBuilder.lowkeyVault;

@SuppressWarnings("resource")
public class LowkeyVaultContainer extends GenericContainer<LowkeyVaultContainer> {

    static final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse("nagyesta/lowkey-vault");
    private static final String DUMMY_USERNAME = "DUMMY";
    private static final String DUMMY_PASSWORD = "DUMMY";
    private static final int CONTAINER_PORT = 8443;
    private static final String HTTPS = "https://";
    private static final String PORT_SEPARATOR = ":";
    private static final String LOCALHOST = "localhost";
    private static final String DOT = ".";

    /**
     * Creates a new instance.
     *
     * @param dockerImageName specified docker image name to run
     * @deprecated Use {@link LowkeyVaultContainerBuilder} instead
     */
    @Deprecated
    public LowkeyVaultContainer(final DockerImageName dockerImageName) {
        this(lowkeyVault(dockerImageName));
    }

    /**
     * Creates a new instance and sets vault names.
     *
     * @param dockerImageName specified docker image name to run
     * @param vaultNames      The names of the vaults we want to initialize at startup
     * @deprecated Use {@link LowkeyVaultContainerBuilder} instead
     */
    @Deprecated
    public LowkeyVaultContainer(final DockerImageName dockerImageName, final Set<String> vaultNames) {
        this(lowkeyVault(dockerImageName).vaultNames(vaultNames));
    }

    /**
     * Constructor for builder.
     *
     * @param containerBuilder Builder instance with the set data.
     */
    LowkeyVaultContainer(final LowkeyVaultContainerBuilder containerBuilder) {
        super(containerBuilder.getDockerImageName());

        containerBuilder.getDockerImageName().assertCompatibleWith(DEFAULT_IMAGE_NAME);
        if (containerBuilder.getHostPort() != null) {
            addFixedExposedPort(containerBuilder.getHostPort(), CONTAINER_PORT);
        } else {
            addExposedPort(CONTAINER_PORT);
        }

        if (containerBuilder.getImportFile() != null) {
            final String absolutePath = containerBuilder.getImportFile().getAbsolutePath();
            logger().info("Using path for import file: '{}'", absolutePath);
            withFileSystemBind(absolutePath, "/import/vaults.json", containerBuilder.getImportFileBindMode());
        }

        if (containerBuilder.getCustomSslCertStore() != null) {
            final String absolutePath = containerBuilder.getCustomSslCertStore().getAbsolutePath();
            logger().info("Using path for custom certificate: '{}'", absolutePath);
            withFileSystemBind(absolutePath, "/config/cert.store", BindMode.READ_ONLY);
        }

        if (containerBuilder.getExternalConfigFile() != null) {
            final String absolutePath = containerBuilder.getExternalConfigFile().getAbsolutePath();
            logger().info("Using path for external configuration: '{}'", absolutePath);
            withFileSystemBind(absolutePath, "/config/application.properties", BindMode.READ_ONLY);
        }

        final List<String> args = new LowkeyVaultArgLineBuilder()
                .vaultNames(Objects.requireNonNullElse(containerBuilder.getVaultNames(), Set.of()))
                .aliases(containerBuilder.getAliasMap())
                .logicalHost(containerBuilder.getLogicalHost())
                .logicalPort(containerBuilder.getLogicalPort())
                .debug(containerBuilder.isDebug())
                .importFile(containerBuilder.getImportFile())
                .customSSLCertificate(containerBuilder.getCustomSslCertStore(),
                        containerBuilder.getCustomSslCertPassword(),
                        containerBuilder.getCustomSslCertType())
                .additionalArgs(containerBuilder.getAdditionalArgs())
                .build();

        if (!args.isEmpty()) {
            withEnv("LOWKEY_ARGS", String.join(" ", args));
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
}
