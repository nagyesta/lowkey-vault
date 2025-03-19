package com.github.nagyesta.lowkeyvault.testcontainers;

import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.lifecycle.Startable;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.util.*;
import java.util.function.Function;

public final class LowkeyVaultContainerBuilder {

    private final DockerImageName dockerImageName;
    private Set<String> vaultNames = Set.of();
    private Map<String, Set<String>> aliasMap = Map.of();
    private File importFile;
    private BindMode importFileBindMode;
    private File customSslCertStore;
    private String customSslCertPassword;
    private StoreType customSslCertType;
    private Integer hostPort;
    private Integer hostTokenPort;
    private Integer logicalPort;
    private String logicalHost;
    private List<String> additionalArgs = List.of();
    private boolean debug;
    private File externalConfigFile;
    private boolean mergeTrustStores = false;
    private final List<ContainerDependency<?>> dependsOnContainers = new ArrayList<>();
    private Function<LowkeyVaultContainer, Map<String, String>> lowkeyVaultSystemPropertySupplier = c -> Map.of();

    /**
     * Creates a new builder instance with the provided image name.
     *
     * @param dockerImageName The name of the container image to use.
     * @return this
     */
    public static LowkeyVaultContainerBuilder lowkeyVault(final String dockerImageName) {
        if (dockerImageName == null) {
            throw new IllegalArgumentException("Image name cannot be null.");
        }
        return lowkeyVault(DockerImageName.parse(dockerImageName));
    }

    /**
     * Creates a new builder instance with the provided image name.
     *
     * @param dockerImageName The name of the container image to use.
     * @return this
     */
    public static LowkeyVaultContainerBuilder lowkeyVault(final DockerImageName dockerImageName) {
        return new LowkeyVaultContainerBuilder(dockerImageName);
    }

    private LowkeyVaultContainerBuilder(final DockerImageName dockerImageName) {
        if (dockerImageName == null) {
            throw new IllegalArgumentException("Image name cannot be null.");
        }
        this.dockerImageName = dockerImageName;
    }

    /**
     * Turns off the automatic registration of vaults. The container will have no vaults in it by default.
     * <br/>
     * Should not be used together with {@link #vaultNames(Set)}.
     *
     * @return this
     */
    public LowkeyVaultContainerBuilder noAutoRegistration() {
        this.vaultNames = Set.of("-");
        return this;
    }

    /**
     * Registers additional vaults using the vault names as prefixes. (See: --LOWKEY_VAULT_NAMES)
     * <br/>
     * Should not be used together with {@link #noAutoRegistration()}.
     *
     * @param vaultNames The names we would like to use.
     * @return this
     */
    public LowkeyVaultContainerBuilder vaultNames(final Set<String> vaultNames) {
        if (vaultNames == null) {
            throw new IllegalArgumentException("Vault names collection cannot be null.");
        }
        this.vaultNames = Set.copyOf(vaultNames);
        return this;
    }

    /**
     * Registers aliases for the defined vaults. (See: --LOWKEY_VAULT_ALIASES)
     *
     * @param aliasMap The aliases we would like to define.
     * @return this
     */
    public LowkeyVaultContainerBuilder vaultAliases(final Map<String, Set<String>> aliasMap) {
        if (aliasMap == null) {
            throw new IllegalArgumentException("Alias map cannot be null.");
        }
        aliasMap.keySet().forEach(host -> {
            if (!host.matches("^[0-9a-z\\-_.]+$")) {
                throw new IllegalArgumentException("Vault host names must match '^[0-9a-z\\-_.]+$'. Found: " + host);
            }
        });
        aliasMap.values().stream().flatMap(Collection::stream).forEach(host -> {
            if (!host.matches("^[0-9a-z\\-_.]+(:[0-9]+|:<port>)?$")) {
                throw new IllegalArgumentException("Vault aliases must match '^[0-9a-z\\-_.]+(:[0-9]+|:<port>)?$'. Found: " + host);
            }
        });
        final Map<String, Set<String>> temp = new TreeMap<>();
        aliasMap.forEach((key, value) -> temp.put(key, Set.copyOf(value)));
        this.aliasMap = Map.copyOf(temp);
        return this;
    }


    /**
     * Specifies a file for import. The file will be attached as a volume and used as import source.
     *
     * @param importFile The file we want to use for import.
     * @return this
     */
    public LowkeyVaultContainerBuilder importFile(final File importFile) {
        return importFile(importFile, BindMode.READ_ONLY);
    }

    /**
     * Specifies a file for import. The file will be attached as a volume and used as import source.
     *
     * @param importFile The file we want to use for import.
     * @param bindMode   Defines whether the file should be read only or read write.
     * @return this
     */
    public LowkeyVaultContainerBuilder importFile(final File importFile, final BindMode bindMode) {
        if (importFile == null) {
            throw new IllegalArgumentException("Import file cannot be null.");
        }
        this.importFile = importFile;
        this.importFileBindMode = bindMode;
        return this;
    }

    /**
     * Specifies an external configuration file to be used for property overrides.
     *
     * @param externalConfigFile The file we want to use as external property source.
     * @return this
     */
    public LowkeyVaultContainerBuilder externalConfigFile(final File externalConfigFile) {
        if (externalConfigFile == null) {
            throw new IllegalArgumentException("External configuration file cannot be null.");
        }
        if (!externalConfigFile.getName().endsWith(".properties")) {
            throw new IllegalArgumentException("External configuration file must be a *.properties file.");
        }
        this.externalConfigFile = externalConfigFile;
        return this;
    }

    /**
     * Provides an external certificate with the necessary configuration to be used as a BYO SSL certificate.
     *
     * @param customSslCert The SSL cert we would like to use as SSL keystore.
     * @param password      The password protecting the key store,
     * @param type          The type of the key store.
     * @return this
     */
    public LowkeyVaultContainerBuilder customSslCertificate(final File customSslCert, final String password, final StoreType type) {
        if (customSslCert == null) {
            throw new IllegalArgumentException("SSL certificate file cannot be null.");
        }
        this.customSslCertStore = customSslCert;
        this.customSslCertPassword = password;
        this.customSslCertType = type;
        return this;
    }

    /**
     * Sets a fixed host port for the port mapping.
     *
     * @param hostPort The host port we want to map the Lowkey Vault HTTPS port.
     * @return this
     * @deprecated No longer recommended, containers start with relaxed port configuration by default,
     * therefore the random port assigned by Testcontainers can be used in Vault URLs without issues.
     */
    @Deprecated
    public LowkeyVaultContainerBuilder hostPort(final int hostPort) {
        if (hostPort < 1) {
            throw new IllegalArgumentException("Host port cannot be zero or negative.");
        }
        this.hostPort = hostPort;
        return this;
    }

    /**
     * Sets a fixed host port for the token endpoint port mapping.
     *
     * @param hostTokenPort The host port we want to map the Lowkey Vault HTTP port.
     * @return this
     */
    public LowkeyVaultContainerBuilder hostTokenPort(final int hostTokenPort) {
        if (hostTokenPort < 1) {
            throw new IllegalArgumentException("Host token port cannot be zero or negative.");
        }
        this.hostTokenPort = hostTokenPort;
        return this;
    }


    /**
     * Sets a logical port the container should use internally. This may appear in the vault URIs.
     * (See: --LOWKEY_IMPORT_TEMPLATE_PORT)
     *
     * @param logicalPort The port we want to expose as HTTPS port from the container.
     * @return this
     */
    public LowkeyVaultContainerBuilder logicalPort(final int logicalPort) {
        if (logicalPort < 1) {
            throw new IllegalArgumentException("Logical port cannot be zero or negative.");
        }
        this.logicalPort = logicalPort;
        return this;
    }

    /**
     * Sets a logical host the container should use. This may appear in the vault URIs if used during imports.
     * (See: --LOWKEY_IMPORT_TEMPLATE_HOST)
     *
     * @param logicalHost The host we want to use during imports.
     * @return this
     */
    public LowkeyVaultContainerBuilder logicalHost(final String logicalHost) {
        if (logicalHost == null) {
            throw new IllegalArgumentException("Logical host cannot be null.");
        }
        this.logicalHost = logicalHost;
        return this;
    }

    /**
     * Specifies additional arguments for the container.
     *
     * @param additionalArgs Args in addition to the ones defined by other builder methods.
     * @return this
     */
    public LowkeyVaultContainerBuilder additionalArgs(final List<String> additionalArgs) {
        if (additionalArgs == null) {
            throw new IllegalArgumentException("Additional argument collection cannot be null.");
        }
        this.additionalArgs = List.copyOf(additionalArgs);
        return this;
    }

    /**
     * Turns on the request debugger.
     *
     * @return this
     */
    public LowkeyVaultContainerBuilder debug() {
        this.debug = true;
        return this;
    }

    /**
     * Tells the {@link LowkeyVaultContainer} to download the SSL certs and merge them with the trust store,
     * When this is used, the resulting trust store (in a temp location) will be set using the relevant
     * System properties for any instructions running in the same JVM after the container start-up.
     *
     * @return this
     */
    public LowkeyVaultContainerBuilder mergeTrustStores() {
        this.mergeTrustStores = true;
        return this;
    }

    /**
     * Defines a dependency on the provided container.
     *
     * @param genericContainer The container we want to depend on.
     * @param <T>              The type of the container we depend on.
     * @return this
     */
    public <T extends Startable> LowkeyVaultContainerBuilder dependsOnContainer(
            final T genericContainer) {
        this.dependsOnContainers.add(new ContainerDependency<>(genericContainer));
        return this;
    }

    /**
     * Defines a dependency on the provided container and sets the supplied map of secrets
     * in the default vault (https://localhost:port) after start-up.
     *
     * @param genericContainer The container we want to depend on.
     * @param secretSupplier   A {@link Function} that can supply a map of secrets.
     * @param <T>              The type of the container we depend on.
     * @return this
     */
    @SuppressWarnings("JavadocLinkAsPlainText")
    public <T extends Startable> LowkeyVaultContainerBuilder dependsOnContainer(
            final T genericContainer,
            final Function<T, Map<String, String>> secretSupplier) {
        this.dependsOnContainers.add(new ContainerDependency<>(genericContainer, secretSupplier));
        return this;
    }

    /**
     * Secret supplier providing Spring Boot secret names suitable for automatically setting up a JDBC datasource.
     *
     * @param <T> The type of the container we depend on.
     * @return this
     */
    public static <T extends JdbcDatabaseContainer<?>> Function<T, Map<String, String>> springJdbcSecretSupplier() {
        return jdbcSecretSupplier("spring-datasource");
    }

    /**
     * Secret supplier providing generic secret names suitable for automatically setting up a JDBC datasource.
     *
     * @param secretPrefix The prefix  want to use for the secrets.
     * @param <T>          The type of the container we depend on.
     * @return this
     */
    public static <T extends JdbcDatabaseContainer<?>> Function<T, Map<String, String>> jdbcSecretSupplier(
            final String secretPrefix) {
        return jdbcContainer -> Map.of(
                secretPrefix + "-url", jdbcContainer.getJdbcUrl(),
                secretPrefix + "-driver-class-name", jdbcContainer.getDriverClassName(),
                secretPrefix + "-username", jdbcContainer.getUsername(),
                secretPrefix + "-password", jdbcContainer.getPassword()
        );
    }

    /**
     * Instructs the {@link LowkeyVaultContainer} to set the specified dynamic system properties after start up.
     *
     * @param systemPropertySupplier The {@link Function} defining the {@link Map} of dynamic properties.
     * @return this
     */
    public LowkeyVaultContainerBuilder setPropertiesAfterStartup(
            final Function<LowkeyVaultContainer, Map<String, String>> systemPropertySupplier) {
        this.lowkeyVaultSystemPropertySupplier = systemPropertySupplier;
        return this;
    }

    /**
     * Spring Boot Cloud Azure Secrets specific system property supplier. Sets the endpoint URL
     * and disables challenge resource verification in the Spring Starter.
     *
     * @param <T> The {@link LowkeyVaultContainer}'s type
     * @return this
     */
    public static <T extends LowkeyVaultContainer> Function<T, Map<String, String>> springCloudAzureKeyVaultPropertySupplier() {
        return lowkeyVaultContainer -> Map.of(
                "spring.cloud.azure.keyvault.secret.property-sources[0].endpoint", lowkeyVaultContainer.getEndpointBaseUrl(),
                "spring.cloud.azure.keyvault.secret.property-sources[0].challenge-resource-verification-enabled", "false"
        );
    }

    /**
     * Builds the container.
     *
     * @return the container
     */
    public LowkeyVaultContainer build() {
        return new LowkeyVaultContainer(this);
    }

    public DockerImageName getDockerImageName() {
        return dockerImageName;
    }

    public Set<String> getVaultNames() {
        return vaultNames;
    }

    public Map<String, Set<String>> getAliasMap() {
        return aliasMap;
    }

    public File getImportFile() {
        return importFile;
    }

    public BindMode getImportFileBindMode() {
        return importFileBindMode;
    }

    public File getCustomSslCertStore() {
        return customSslCertStore;
    }

    public File getExternalConfigFile() {
        return externalConfigFile;
    }

    public String getCustomSslCertPassword() {
        return customSslCertPassword;
    }

    public StoreType getCustomSslCertType() {
        return customSslCertType;
    }

    public Integer getHostPort() {
        return hostPort;
    }

    public Integer getHostTokenPort() {
        return hostTokenPort;
    }

    public Integer getLogicalPort() {
        return logicalPort;
    }

    public String getLogicalHost() {
        return logicalHost;
    }

    public List<String> getAdditionalArgs() {
        return additionalArgs;
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isMergeTrustStores() {
        return mergeTrustStores;
    }

    public List<ContainerDependency<?>> getDependsOnContainers() {
        return List.copyOf(dependsOnContainers);
    }

    public Function<LowkeyVaultContainer, Map<String, String>> getLowkeyVaultSystemPropertySupplier() {
        return lowkeyVaultSystemPropertySupplier;
    }
}
