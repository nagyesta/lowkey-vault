![LowkeyVault](../.github/assets/LowkeyVault-logo-full.png)

[![GitHub license](https://img.shields.io/github/license/nagyesta/lowkey-vault?color=informational)](https://raw.githubusercontent.com/nagyesta/lowkey-vault/main/LICENSE)
[![Java version](https://img.shields.io/badge/Java%20version-11-yellow?logo=java)](https://img.shields.io/badge/Java%20version-11-yellow?logo=java)
[![latest-release](https://img.shields.io/github/v/tag/nagyesta/lowkey-vault?color=blue&logo=git&label=releases&sort=semver)](https://github.com/nagyesta/lowkey-vault/releases)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.nagyesta.lowkey-vault/lowkey-vault-app?logo=apache-maven)](https://search.maven.org/search?q=com.github.nagyesta.lowkey-vault)
[![Docker Hub](https://img.shields.io/docker/v/nagyesta/lowkey-vault?sort=semver&arch=amd64&logo=docker&label=amd64)](https://hub.docker.com/r/nagyesta/lowkey-vault)
[![Docker Hub](https://img.shields.io/docker/v/nagyesta/lowkey-vault?sort=date&arch=arm64&logo=docker&label=multi-arch)](https://hub.docker.com/r/nagyesta/lowkey-vault)

[![JavaCI](https://img.shields.io/github/actions/workflow/status/nagyesta/lowkey-vault/gradle.yml?logo=github&branch=main)](https://github.com/nagyesta/lowkey-vault/actions/workflows/gradle.yml)
[![codecov](https://img.shields.io/codecov/c/github/nagyesta/lowkey-vault?label=Coverage&flag=testcontainers&token=3ZZ9Q4S5WW)](https://app.codecov.io/gh/nagyesta/lowkey-vault?flags%5B0%5D=testcontainers)
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
<!-- 
  In case you wish to use the provided Lowkey Vault Client too.
  This is necessary for some advanced features like the client 
  factory or setting secrets when the trust store is not merged. 
-->
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
//This is necessary for some advanced features like the client 
//factory or setting secrets when the trust store is not merged.
testImplementation 'com.github.nagyesta.lowkey-vault:lowkey-vault-client:+'
```

### Creating a container

The recommended way of creating a container is by using [LowkeyVaultContainerBuilder](src/main/java/com/github/nagyesta/lowkeyvault/testcontainers/LowkeyVaultContainerBuilder.java).

#### Example auto-registering a vault

In this example we would like to register the ```https://default.localhost:8443``` vault and let the container start using a random
port on the host machine.

```java
import org.testcontainers.utility.DockerImageName;
import com.github.nagyesta.lowkeyvault.testcontainers.LowkeyVaultContainer;
import static com.github.nagyesta.lowkeyvault.testcontainers.LowkeyVaultContainerBuilder.lowkeyVault;

class Test {
    public LowkeyVaultContainer startVault() {
        //Please consider using latest image regardless of the value in the example
        final DockerImageName imageName = DockerImageName.parse("nagyesta/lowkey-vault:<version>");
        final LowkeyVaultContainer lowkeyVaultContainer = lowkeyVault(imageName)
                .vaultNames(Set.of("default"))
                .build()
                .withImagePullPolicy(PullPolicy.defaultPolicy());
        lowkeyVaultContainer.start();
        return lowkeyVaultContainer;
    }
}
```

##### ARM builds

> [!TIP]
> Lowkey Vault offers a multi-arch image variant too. You can find the relevant project [here](https://github.com/nagyesta/lowkey-vault-docker-buildx).


#### Example importing contents from file

In this example we are importing a file including the placeholder specific configuration and setting additional parameters 
to use a specific port on the host machine and disable automatic vault registration.

```java
import org.testcontainers.utility.DockerImageName;
import com.github.nagyesta.lowkeyvault.testcontainers.LowkeyVaultContainer;
import static com.github.nagyesta.lowkeyvault.testcontainers.LowkeyVaultContainerBuilder.lowkeyVault;

class Test {
    public LowkeyVaultContainer startVault(final File importFile) {
        //Please consider using latest image regardless of the value in the example
        final DockerImageName imageName = DockerImageName.parse("nagyesta/lowkey-vault:<version>");
        final LowkeyVaultContainer lowkeyVaultContainer = lowkeyVault(imageName)
                .noAutoRegistration()  
                .importFile(importFile, BindMode.READ_ONLY)
                .logicalPort(8443)
                .logicalHost("127.0.0.1")
                .build()
                .withImagePullPolicy(PullPolicy.defaultPolicy());
        lowkeyVaultContainer.start();
        return lowkeyVaultContainer;
    }
}

```

> [!NOTE]
> Since `v2.7.0`, the container can use a dynamically allocated port thanks to the relaxed port matching feature. This will ignore the port number when searching for a vault based on the request authority (essentially only matching based
on the request's hostname).

#### Example using your own certificate file

In this example we are importing a custom SSL certificate from a key store file.

```java
import org.testcontainers.utility.DockerImageName;
import com.github.nagyesta.lowkeyvault.testcontainers.LowkeyVaultContainer;
import static com.github.nagyesta.lowkeyvault.testcontainers.LowkeyVaultContainerBuilder.lowkeyVault;

class Test {
    public LowkeyVaultContainer startVault(final File certFile) {
        //Please consider using latest image regardless of the value in the example
        final DockerImageName imageName = DockerImageName.parse("nagyesta/lowkey-vault:<version>");
        final LowkeyVaultContainer lowkeyVaultContainer = lowkeyVault(imageName)
                .noAutoRegistration()
                .customSslCertificate(certFile, "password", StoreType.JKS)
                .hostPort(8443)
                .build()
                .withImagePullPolicy(PullPolicy.defaultPolicy());
        lowkeyVaultContainer.start();
        return lowkeyVaultContainer;
    }
}

```

#### Example using additional startup arguments

In this example we are passing through additional arguments to the container.

```java
import org.testcontainers.utility.DockerImageName;
import com.github.nagyesta.lowkeyvault.testcontainers.LowkeyVaultContainer;
import static com.github.nagyesta.lowkeyvault.testcontainers.LowkeyVaultContainerBuilder.lowkeyVault;

class Test {
    public LowkeyVaultContainer startVault() {
        //Please consider using latest image regardless of the value in the example
        final DockerImageName imageName = DockerImageName.parse("nagyesta/lowkey-vault:<version>");
        final LowkeyVaultContainer lowkeyVaultContainer = lowkeyVault(imageName)
                .additionalArgs(List.of("--logging.level.root=INFO"))
                .build()
                .withImagePullPolicy(PullPolicy.defaultPolicy());
        lowkeyVaultContainer.start();
        return lowkeyVaultContainer;
    }
}

```

#### Example using aliases

In this example we are starting Lowkey vault with additional aliases (```https://alias1``` and ```https://alias2:8443```)
defined for the default vault named `https://localhost:8443`.

```java
import org.testcontainers.utility.DockerImageName;
import com.github.nagyesta.lowkeyvault.testcontainers.LowkeyVaultContainer;
import static com.github.nagyesta.lowkeyvault.testcontainers.LowkeyVaultContainerBuilder.lowkeyVault;

class Test {
    public LowkeyVaultContainer startVault() {
        //Please consider using latest image regardless of the value in the example
        final DockerImageName imageName = DockerImageName.parse("nagyesta/lowkey-vault:<version>");
        final LowkeyVaultContainer lowkeyVaultContainer = lowkeyVault(imageName)
                .vaultAliases(Map.of("localhost", Set.of("alias1", "alias2:8443")))
                .build()
                .withImagePullPolicy(PullPolicy.defaultPolicy());
        lowkeyVaultContainer.start();
        return lowkeyVaultContainer;
    }
}

```

#### Example using Spring

In this example we are starting Lowkey vault with the Spring Cloud Azure Starter using a JDBC container.

```java
import org.testcontainers.utility.DockerImageName;
import com.github.nagyesta.lowkeyvault.testcontainers.LowkeyVaultContainer;
import static com.github.nagyesta.lowkeyvault.testcontainers.LowkeyVaultContainerBuilder.*;

class Test {
    public LowkeyVaultContainer startVault(final JdbcDatabaseContainer jdbcContainer) {
        //Please consider using latest image regardless of the value in the example
        final DockerImageName imageName = DockerImageName.parse("nagyesta/lowkey-vault:<version>");
        final LowkeyVaultContainer lowkeyVaultContainer = lowkeyVault(imageName)
                .hostTokenPort(18080) //ses the token port as it is needed for Managed Identity simulation
                .dependsOnContainer(jdbcContainer, springJdbcSecretSupplier())
                .mergeTrustStores()
                .setPropertiesAfterStartup(springCloudAzureKeyVaultPropertySupplier())
                .build();
        lowkeyVaultContainer.start();
        return lowkeyVaultContainer;
    }
}

```

### Other examples

* [Generic](src/test/java/com/github/nagyesta/lowkeyvault/testcontainers/LowkeyVaultContainerVanillaTest.java)
* [JUnit Jupiter](src/test/java/com/github/nagyesta/lowkeyvault/testcontainers/LowkeyVaultContainerJupiterTest.java)
* [Lowkey Vault Example](https://github.com/nagyesta/lowkey-vault-example)
