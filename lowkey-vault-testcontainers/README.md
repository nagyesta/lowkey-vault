![LowkeyVault](../.github/assets/LowkeyVault-logo-full.png)

[![GitHub license](https://img.shields.io/github/license/nagyesta/lowkey-vault?color=informational)](https://raw.githubusercontent.com/nagyesta/lowkey-vault/main/LICENSE)
[![Java version](https://img.shields.io/badge/Java%20version-11-yellow?logo=java)](https://img.shields.io/badge/Java%20version-11-yellow?logo=java)
[![latest-release](https://img.shields.io/github/v/tag/nagyesta/lowkey-vault?color=blue&logo=git&label=releases&sort=semver)](https://github.com/nagyesta/lowkey-vault/releases)
[![Docker Hub](https://img.shields.io/docker/v/nagyesta/lowkey-vault?label=docker%20hub&logo=docker&sort=semver)](https://hub.docker.com/repository/docker/nagyesta/lowkey-vault)
[![JavaCI](https://img.shields.io/github/workflow/status/nagyesta/lowkey-vault/JavaCI?logo=github)](https://img.shields.io/github/workflow/status/nagyesta/lowkey-vault/JavaCI?logo=github)
[![codecov](https://img.shields.io/codecov/c/github/nagyesta/lowkey-vault?label=Coverage&token=3ZZ9Q4S5WW)](https://img.shields.io/codecov/c/github/nagyesta/lowkey-vault?label=Coverage&token=3ZZ9Q4S5WW)
[![badge-abort-mission-armed-green](https://raw.githubusercontent.com/nagyesta/abort-mission/wiki_assets/.github/assets/badge-abort-mission-armed-green.svg)](https://github.com/nagyesta/abort-mission)

# Lowkey Vault - Testcontainers

This is the root of the Java Testcontainers support library. Visit the [Readme](../README.md) in the repo root for more information about the
project in general.

## Usage

### Dependency

The [Testcontainers](https://testcontainers.org/) specific dependencies can be found below. The following examples assume that
[Azure Key Vault Key client](https://docs.microsoft.com/en-us/azure/key-vault/keys/quick-create-java)
and [Azure Key Vault Secret client](https://docs.microsoft.com/en-us/azure/key-vault/secrets/quick-create-java), are already on your
classpath.

#### Maven

```xml

<dependency>
    <groupId>com.github.nagyesta.lowkey-vault</groupId>
    <artifactId>lowkey-vault-testcontainers</artifactId>
    <version>RELEASE</version>
    <scope>test</scope>
</dependency>
<!-- In case you wish to use the provided Lowkey Vault Client too -->
<dependency>
    <groupId>com.github.nagyesta.lowkey-vault</groupId>
    <artifactId>lowkey-vault-client</artifactId>
    <version>RELEASE</version>
    <scope>test</scope>
</dependency>
```

#### Gradle

```groovy
testImplementation 'com.github.nagyesta.lowkey-vault:lowkey-vault-testcontainers:+'
//In case you wish to use the provided Lowkey Vault Client too
testImplementation 'com.github.nagyesta.lowkey-vault:lowkey-vault-client:+'
```

### Examples

* [Generic](src/test/java/com/github/nagyesta/lowkeyvault/testcontainers/LowkeyVaultContainerVanillaTest.java)
* [JUnit Jupiter](src/test/java/com/github/nagyesta/lowkeyvault/testcontainers/LowkeyVaultContainerJupiterTest.java)
