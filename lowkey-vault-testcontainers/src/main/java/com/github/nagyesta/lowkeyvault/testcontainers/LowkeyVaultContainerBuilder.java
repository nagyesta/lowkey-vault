package com.github.nagyesta.lowkeyvault.testcontainers;

import org.testcontainers.containers.BindMode;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.util.*;

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
    private Integer logicalPort;
    private String logicalHost;
    private List<String> additionalArgs = List.of();
    private boolean debug;
    private File externalConfigFile;

    public static LowkeyVaultContainerBuilder lowkeyVault(final DockerImageName dockerImageName) {
        return new LowkeyVaultContainerBuilder(dockerImageName);
    }

    private LowkeyVaultContainerBuilder(final DockerImageName dockerImageName) {
        if (dockerImageName == null) {
            throw new IllegalArgumentException("Image name cannot be null.");
        }
        this.dockerImageName = dockerImageName;
    }

    public LowkeyVaultContainerBuilder noAutoRegistration() {
        this.vaultNames = Set.of("-");
        return this;
    }

    public LowkeyVaultContainerBuilder vaultNames(final Set<String> vaultNames) {
        if (vaultNames == null) {
            throw new IllegalArgumentException("Vault names collection cannot be null.");
        }
        this.vaultNames = Set.copyOf(vaultNames);
        return this;
    }

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

    public LowkeyVaultContainerBuilder importFile(final File importFile) {
        return importFile(importFile, BindMode.READ_ONLY);
    }

    public LowkeyVaultContainerBuilder importFile(final File importFile, final BindMode bindMode) {
        if (importFile == null) {
            throw new IllegalArgumentException("Import file cannot be null.");
        }
        this.importFile = importFile;
        this.importFileBindMode = bindMode;
        return this;
    }

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

    public LowkeyVaultContainerBuilder customSslCertificate(final File customSslCert, final String password, final StoreType type) {
        if (customSslCert == null) {
            throw new IllegalArgumentException("SSL certificate file cannot be null.");
        }
        this.customSslCertStore = customSslCert;
        this.customSslCertPassword = password;
        this.customSslCertType = type;
        return this;
    }

    public LowkeyVaultContainerBuilder hostPort(final int hostPort) {
        if (hostPort < 1) {
            throw new IllegalArgumentException("Host port cannot be zero or negative.");
        }
        this.hostPort = hostPort;
        return this;
    }

    public LowkeyVaultContainerBuilder logicalPort(final int logicalPort) {
        if (logicalPort < 1) {
            throw new IllegalArgumentException("Logical port cannot be zero or negative.");
        }
        this.logicalPort = logicalPort;
        return this;
    }

    public LowkeyVaultContainerBuilder logicalHost(final String logicalHost) {
        if (logicalHost == null) {
            throw new IllegalArgumentException("Logical host cannot be null.");
        }
        this.logicalHost = logicalHost;
        return this;
    }

    public LowkeyVaultContainerBuilder additionalArgs(final List<String> additionalArgs) {
        if (additionalArgs == null) {
            throw new IllegalArgumentException("Additional argument collection cannot be null.");
        }
        this.additionalArgs = List.copyOf(additionalArgs);
        return this;
    }

    public LowkeyVaultContainerBuilder debug() {
        this.debug = true;
        return this;
    }

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
}
