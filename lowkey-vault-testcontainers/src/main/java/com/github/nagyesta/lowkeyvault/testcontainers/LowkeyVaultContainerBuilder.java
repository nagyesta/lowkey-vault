package com.github.nagyesta.lowkeyvault.testcontainers;

import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.util.Set;

public final class LowkeyVaultContainerBuilder {

    private final DockerImageName dockerImageName;
    private Set<String> vaultNames = Set.of();
    private File importFile;
    private Integer hostPort;
    private Integer logicalPort;
    private String logicalHost;

    private boolean debug;

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
        if (vaultNames.contains(null)) {
            throw new IllegalArgumentException("Vault names collection cannot contain null.");
        }
        this.vaultNames = Set.copyOf(vaultNames);
        return this;
    }

    public LowkeyVaultContainerBuilder importFile(final File importFile) {
        if (importFile == null) {
            throw new IllegalArgumentException("Import file cannot be null.");
        }
        this.importFile = importFile;
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

    public File getImportFile() {
        return importFile;
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

    public boolean isDebug() {
        return debug;
    }
}
