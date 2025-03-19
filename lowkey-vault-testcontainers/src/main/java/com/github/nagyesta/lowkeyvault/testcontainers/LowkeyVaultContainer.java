package com.github.nagyesta.lowkeyvault.testcontainers;

import com.azure.core.credential.BasicAuthenticationCredential;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.SecretServiceVersion;
import org.slf4j.Logger;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.*;
import java.util.function.Function;

import static com.github.nagyesta.lowkeyvault.testcontainers.LowkeyVaultContainerBuilder.lowkeyVault;

@SuppressWarnings("resource")
public class LowkeyVaultContainer extends GenericContainer<LowkeyVaultContainer> {

    static final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse("nagyesta/lowkey-vault");
    private static final String DUMMY_USERNAME = "DUMMY";
    private static final String DUMMY_PASSWORD = "DUMMY";
    private static final int CONTAINER_PORT = 8443;
    private static final int CONTAINER_TOKEN_PORT = 8080;
    @SuppressWarnings("HttpUrlsUsage")
    private static final String HTTP = "http://";
    private static final String HTTPS = "https://";
    private static final String PORT_SEPARATOR = ":";
    private static final String LOCALHOST = "localhost";
    private static final String DOT = ".";
    private static final String TOKEN_ENDPOINT_PATH = "/metadata/identity/oauth2/token";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Integer logicalPort;
    private final boolean mergeTrustStores;
    private final List<ContainerDependency<?>> dependsOnContainers;
    private final Function<LowkeyVaultContainer, Map<String, String>> lowkeyVaultSystemPropertySupplier;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<KeyStoreMerger> keyStoreMerger = Optional.empty();

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

        recommendMultiArchImageIfApplicable(logger(),
                containerBuilder.getDockerImageName(),
                DockerClientFactory.instance().client().versionCmd().exec().getArch());

        if (containerBuilder.getHostPort() != null) {
            addFixedExposedPort(containerBuilder.getHostPort(), CONTAINER_PORT);
        } else {
            addExposedPort(CONTAINER_PORT);
        }
        if (containerBuilder.getHostTokenPort() != null) {
            addFixedExposedPort(containerBuilder.getHostTokenPort(), CONTAINER_TOKEN_PORT);
        } else {
            addExposedPort(CONTAINER_TOKEN_PORT);
        }
        this.logicalPort = containerBuilder.getLogicalPort();

        if (containerBuilder.getImportFile() != null) {
            final var absolutePath = containerBuilder.getImportFile().getAbsolutePath();
            logger().info("Using path for import file: '{}'", absolutePath);
            withFileSystemBind(absolutePath, "/import/vaults.json", containerBuilder.getImportFileBindMode());
        }

        if (containerBuilder.getCustomSslCertStore() != null) {
            final var absolutePath = containerBuilder.getCustomSslCertStore().getAbsolutePath();
            logger().info("Using path for custom certificate: '{}'", absolutePath);
            withFileSystemBind(absolutePath, "/config/cert.store", BindMode.READ_ONLY);
        }

        if (containerBuilder.getExternalConfigFile() != null) {
            final var absolutePath = containerBuilder.getExternalConfigFile().getAbsolutePath();
            logger().info("Using path for external configuration: '{}'", absolutePath);
            withFileSystemBind(absolutePath, "/config/application.properties", BindMode.READ_ONLY);
        }

        final var args = new LowkeyVaultArgLineBuilder()
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

        withEnv("LOWKEY_ARGS", String.join(" ", args));
        waitingFor(Wait.forLogMessage("(?s).*Started LowkeyVaultApp.*$", 1));
        mergeTrustStores = containerBuilder.isMergeTrustStores();
        dependsOnContainers = containerBuilder.getDependsOnContainers();
        dependsOnContainers.stream()
                .map(ContainerDependency::getContainer)
                .forEach(this::dependsOn);
        lowkeyVaultSystemPropertySupplier = Objects.requireNonNullElse(
                containerBuilder.getLowkeyVaultSystemPropertySupplier(),
                c -> Map.of());
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
     * Returns the URL of the token endpoint.
     *
     * @return token endpoint base URL.
     */
    public String getTokenEndpointBaseUrl() {
        return HTTP + LOCALHOST + PORT_SEPARATOR + getMappedPort(CONTAINER_TOKEN_PORT);
    }

    /**
     * Returns the full URL of the token endpoint.
     *
     * @return full token endpoint URL.
     */
    public String getTokenEndpointUrl() {
        return getTokenEndpointBaseUrl() + TOKEN_ENDPOINT_PATH;
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
        return LOCALHOST + PORT_SEPARATOR + getLogicalPort();
    }

    /**
     * Returns the authority of the URL belonging to the vault with a given name.
     *
     * @param vaultName the name of the vault
     * @return authority of the given vault base URL.
     */
    public String getVaultAuthority(final String vaultName) {
        return Objects.requireNonNull(vaultName) + DOT + LOCALHOST + PORT_SEPARATOR + getLogicalPort();
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
     * Returns the base URL of the endpoint we can use to access the container.
     *
     * @return endpoint base URL.
     */
    public String getEndpointBaseUrl() {
        return HTTPS + getEndpointAuthority();
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

    /**
     * Returns a key store containing the default certificate shipped with Lowkey Vault.
     *
     * @return keyStore
     */
    public KeyStore getDefaultKeyStore() {
        final var request = HttpRequest.newBuilder()
                .uri(URI.create(getTokenEndpointBaseUrl() + "/metadata/default-cert/lowkey-vault.p12"))
                .GET()
                .build();
        try {
            final var keyStoreBytes = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray())
                    .body();
            final var keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(new ByteArrayInputStream(keyStoreBytes), getDefaultKeyStorePassword().toCharArray());
            return keyStore;
        } catch (final Exception e) {
            throw new IllegalStateException("Failed to get default key store", e);
        }
    }

    /**
     * Returns password protecting the default certificate shipped with Lowkey Vault.
     *
     * @return password
     */
    public String getDefaultKeyStorePassword() {
        final var request = HttpRequest.newBuilder()
                .uri(URI.create(getTokenEndpointBaseUrl() + "/metadata/default-cert/password"))
                .GET()
                .build();
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)).body();
        } catch (final Exception e) {
            throw new IllegalStateException("Failed to get default key store password", e);
        }
    }

    /**
     * Evaluates whether the currently used image is the recommended one for the host architecture.
     * Prints warning log messages if it would be recommended to use multi-arch images instead.
     *
     * @param logger          The logger where we want to print the recommendation
     * @param dockerImageName The name of the current docker image
     * @param hostArch        The host architecture
     */
    protected void recommendMultiArchImageIfApplicable(
            final Logger logger,
            final DockerImageName dockerImageName,
            final String hostArch) {
        dockerImageName.assertCompatibleWith(DEFAULT_IMAGE_NAME);
        final var hostArchIsNotAmd64 = !"amd64".equals(hostArch);
        final var defaultImageUsed = DEFAULT_IMAGE_NAME.getUnversionedPart().equals(dockerImageName.getUnversionedPart());
        final var versionPart = dockerImageName.getVersionPart();
        final var imageIsNotMultiArch = !versionPart.contains("-ubi9-minimal");
        if (defaultImageUsed && hostArchIsNotAmd64 && imageIsNotMultiArch) {
            logger.warn("An amd64 image is detected with non-amd64 ({}) host.", hostArch);
            logger.warn("Please consider using a multi-arch image, like: {}-ubi9-minimal", versionPart);
            logger.warn("See more information: https://github.com/nagyesta/lowkey-vault/tree/main/lowkey-vault-docker#arm-builds");
        }
    }

    /**
     * Provides a {@link LowkeyVaultClientFactory} instance to help with creating clients.
     *
     * @return factory
     */
    public LowkeyVaultClientFactory getClientFactory() {
        return new LowkeyVaultClientFactory(this);
    }

    @Override
    public void start() {
        super.start();
        mergeTrustStoresIfNecessary();
        autoSetDependencySecrets();
        lowkeyVaultSystemPropertySupplier.apply(this).forEach((key, value) -> {
            logger().info("Setting system property '{}' to '{}'", key, value);
            System.setProperty(key, value);
        });
    }

    @Override
    public void stop() {
        super.stop();
        keyStoreMerger.ifPresent(KeyStoreMerger::close);
    }

    private int getLogicalPort() {
        return Optional.ofNullable(logicalPort)
                .orElse(getMappedPort(CONTAINER_PORT));
    }

    private void autoSetDependencySecrets() {
        if (!dependsOnContainers.isEmpty()) {
            final var secretClient = getSecretClient();
            dependsOnContainers.forEach(dependency -> {
                final var secrets = dependency.getSecrets();
                secrets.forEach((name, value) -> {
                    logger().info("Saving secret '{}' with value '{}' to default vault", name, value);
                    secretClient.setSecret(name, value);
                });
            });
        }
    }

    private SecretClient getSecretClient() {
        final SecretClient secretClient;
        if (shouldUseOfficialAzureClients()) {
            logger().info("Using official Azure Secret Client to save secrets to default vault.");
            secretClient = new SecretClientBuilder()
                    .vaultUrl(getDefaultVaultBaseUrl())
                    .disableChallengeResourceVerification()
                    .credential(new BasicAuthenticationCredential(getUsername(), getPassword()))
                    .serviceVersion(SecretServiceVersion.V7_5)
                    .buildClient();
        } else {
            logger().info("Using Lowkey Vault Secret Client to save secrets to default vault.");
            secretClient = getClientFactory()
                    .getSecretClientBuilderForDefaultVault()
                    .buildClient();
        }
        return secretClient;
    }

    private boolean shouldUseOfficialAzureClients() {
        return mergeTrustStores || lowkeyVaultClientIsNotPresent();
    }

    private boolean lowkeyVaultClientIsNotPresent() {
        try {
            Class.forName("com.github.nagyesta.lowkeyvault.http.ApacheHttpClient");
            return false;
        } catch (final ClassNotFoundException e) {
            return true;
        }
    }

    private void mergeTrustStoresIfNecessary() {
        if (!mergeTrustStores) {
            logger().debug("Not merging trust stores.");
            return;
        }
        logger().info("Merging trust stores");
        final var defaultKeyStore = getDefaultKeyStore();
        final var defaultKeyStorePassword = getDefaultKeyStorePassword();
        final var merger = new KeyStoreMerger(defaultKeyStore, defaultKeyStorePassword.toCharArray());
        merger.mergeDefaultTrustStore();
        keyStoreMerger = Optional.of(merger);
    }
}
