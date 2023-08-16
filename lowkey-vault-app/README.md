![LowkeyVault](../.github/assets/LowkeyVault-logo-full.png)

[![GitHub license](https://img.shields.io/github/license/nagyesta/lowkey-vault?color=informational)](https://raw.githubusercontent.com/nagyesta/lowkey-vault/main/LICENSE)
[![Java version](https://img.shields.io/badge/Java%20version-11-yellow?logo=java)](https://img.shields.io/badge/Java%20version-11-yellow?logo=java)
[![latest-release](https://img.shields.io/github/v/tag/nagyesta/lowkey-vault?color=blue&logo=git&label=releases&sort=semver)](https://github.com/nagyesta/lowkey-vault/releases)
[![Docker Hub](https://img.shields.io/docker/v/nagyesta/lowkey-vault?label=docker%20hub&logo=docker&sort=semver)](https://hub.docker.com/r/nagyesta/lowkey-vault)
[![JavaCI](https://img.shields.io/github/actions/workflow/status/nagyesta/lowkey-vault/gradle.yml?logo=github&branch=main)](https://github.com/nagyesta/lowkey-vault/actions/workflows/gradle.yml)
[![codecov](https://img.shields.io/codecov/c/github/nagyesta/lowkey-vault?label=Coverage&flag=app&token=3ZZ9Q4S5WW)](https://img.shields.io/codecov/c/github/nagyesta/lowkey-vault?label=Coverage&flag=app&token=3ZZ9Q4S5WW)
[![badge-abort-mission-armed-green](https://raw.githubusercontent.com/nagyesta/abort-mission/wiki_assets/.github/assets/badge-abort-mission-armed-green.svg)](https://github.com/nagyesta/abort-mission)

# Lowkey Vault - App

This is the root of the Java app. Visit the [Readme](../README.md) in the repo root for more information about the project in general.

## Startup parameters

### Log requests

In order to support debugging integration, Lowkey Vault can log request data. To turn on this feature,
you must pass ```--LOWKEY_DEBUG_REQUEST_LOG=true``` as startup argument:

```shell
java -jar lowkey-vault-app-<version>.jar --LOWKEY_DEBUG_REQUEST_LOG=true
```

### Non-default vaults

In case you wish to use more than one vaults, you should consider registering additional vaults using
the ```--LOWKEY_VAULT_NAMES=<name1>,<name2>``` comma separated format. This will register the
```https://<name1>.localhost:<server.port>``` and ```https://<name2>.localhost:<server.port>``` vaults
in the aforementioned example. You can pass any number of vault prefixes as long as you have enough RAM:

```shell
java -jar lowkey-vault-app-<version>.jar --LOWKEY_VAULT_NAMES=name1,name2
```

A handful of vaults are available by default. These are
configured [here](src/main/java/com/github/nagyesta/lowkeyvault/AppConfiguration.java#L39).

The example above would register the following vaults as it can be seen in the logs as well:

```
Creating vault for URI: https://127.0.0.1:8443
Creating vault for URI: https://localhost:8443
Creating vault for URI: https://default.lowkey-vault:8443
Creating vault for URI: https://default.lowkey-vault.local:8443
Creating vault for URI: https://name1.localhost:8443
Creating vault for URI: https://name2.localhost:8443
```

If you wish to turn off the automatic vault registration feature, simply pass ```--LOWKEY_VAULT_NAMES=-```:

```shell
java -jar lowkey-vault-app-<version>.jar --LOWKEY_VAULT_NAMES=-
```

#### Defining vault aliases for auto-registered vaults

Since ```v1.13.0```

If you are using the automatically registered vaults, you can add aliases to them by using the ```--LOWKEY_VAULT_ALIASES```
argument.

1. This argument is containing the comma (```,```) separated list of pairs.
2. Each pair should
    1. start with the host name of an auto-registered vault without the port suffix
    2. then contain an equals sign (```=```)
    3. then define the host authority of the alias vault in one of the following formats
        1. ```hostname``` (e.g. ```localhost```),
           meaning that we want to register an alias to the ```:443``` port of the host defined in the ```hostname``` part
        2. ```hostname:port``` (e.g. ```localhost:8443```)
           meaning, that we want to register an alias to exactly that host and port, which is defined
        3. ```hostname:<port>``` (e.g. ```localhost:<port>```)
           meaning that we want to register an alias to the the host defined in the ```hostname``` using the port set with
           ```--server.port```

```shell
java -jar lowkey-vault-app-<version>.jar --LOWKEY_VAULT_NAMES="name1" --LOWKEY_VAULT_ALIASES="name1.localhost=alias.localhost,localhost=example:<port>"
```

This command will result in the following aliases as seen in the logs:

```
Updating aliases of: https://name1.localhost:8443 , adding: https://alias.localhost, removing: null
Updating aliases of: https://localhost:8443 , adding: https://example:8443, removing: null
```

### Custom port use

In order to avoid using the reserved `8443` port, we need to tell Lowkey Vault to use a different one instead.
Set `--server.port=<port>` as an argument as usual with Spring Boot apps:

```shell
java -jar lowkey-vault-app-<version>.jar --server.port=8443
```

### Importing vault content at startup

When you need to automatically import the contents of the vaults form a previously created JSON export, you can
use a few parameters to customise the way import is happening.

| Property                      | Default         | Description                                                                                                  |
|-------------------------------|-----------------|--------------------------------------------------------------------------------------------------------------|
| `LOWKEY_IMPORT_LOCATION`      | `<null>`        | The JSON file we want to import. If left on default, or the file cannot be read, the import will not happen. |
| `LOWKEY_IMPORT_TEMPLATE_HOST` | `localhost`     | The host name we want to use for replacing `{{host}}` placeholders in the JSON export (if there are any).    |
| `LOWKEY_IMPORT_TEMPLATE_PORT` | `<server.port>` | The port number we want to use for replacing `{{port}}` placeholders in the JSON export (if there are any).  |

Similarly to how ```LOWKEY_IMPORT_TEMPLATE_HOST``` and ```LOWKEY_IMPORT_TEMPLATE_PORT``` are replacing placeholders,
we can use ```{{now <seconds>}}``` placeholders (with positive or negative values as well) which will be replaced
with the ```<UTC_Epoch_seconds_now> + <seconds>``` formula. This can allow you to use relative timestamps, in case
your tests need a key of certain age relative to the time of the test execution.

Example:

```shell
java -jar lowkey-vault-app-<version>.jar --LOWKEY_IMPORT_LOCATION=export.json --LOWKEY_IMPORT_TEMPLATE_HOST=127.0.0.1 --LOWKEY_IMPORT_TEMPLATE_PORT=443
```

### External configuration

Since Lowkey Vault is a Spring Boot application, the default mechanism for Spring Boot external configuration can work as well. For example,
if there is a ./config/application.properties file relative to the folder where you are running Lowkey Vault, the contents will be picked up
automatically. For more information please see [the Spring Boot documentation](https://docs.spring.io/spring-boot/docs/2.1.9.RELEASE/reference/html/boot-features-external-config.html#boot-features-external-config-application-property-files).
