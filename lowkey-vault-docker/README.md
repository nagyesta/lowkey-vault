![LowkeyVault](../.github/assets/LowkeyVault-logo-full.png)

[![GitHub license](https://img.shields.io/github/license/nagyesta/lowkey-vault?color=informational)](https://raw.githubusercontent.com/nagyesta/lowkey-vault/main/LICENSE)
[![Java version](https://img.shields.io/badge/Java%20version-21-yellow?logo=java)](https://img.shields.io/badge/Java%20version-21-yellow?logo=java)
[![latest-release](https://img.shields.io/github/v/tag/nagyesta/lowkey-vault?color=blue&logo=git&label=releases&sort=semver)](https://github.com/nagyesta/lowkey-vault/releases)
[![Docker Hub](https://img.shields.io/docker/v/nagyesta/lowkey-vault?sort=semver&arch=amd64&logo=docker&label=amd64)](https://hub.docker.com/r/nagyesta/lowkey-vault)
[![Docker Hub](https://img.shields.io/docker/v/nagyesta/lowkey-vault?sort=date&arch=arm64&logo=docker&label=multi-arch)](https://hub.docker.com/r/nagyesta/lowkey-vault)

[![JavaCI](https://img.shields.io/github/actions/workflow/status/nagyesta/lowkey-vault/gradle.yml?logo=github&branch=main)](https://github.com/nagyesta/lowkey-vault/actions/workflows/gradle.yml)
[![Sonar Coverage](https://img.shields.io/sonar/coverage/nagyesta_lowkey-vault?server=https%3A%2F%2Fsonarcloud.io&logo=sonarcloud&logoColor=white)](https://sonarcloud.io/summary/new_code?id=nagyesta_lowkey-vault)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=nagyesta_lowkey-vault&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=nagyesta_lowkey-vault)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=nagyesta_lowkey-vault&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=nagyesta_lowkey-vault)
[![Docker Pulls](https://img.shields.io/docker/pulls/nagyesta/lowkey-vault?logo=docker)](https://hub.docker.com/r/nagyesta/lowkey-vault)
[![badge-abort-mission-armed-green](https://raw.githubusercontent.com/nagyesta/abort-mission/wiki_assets/.github/assets/badge-abort-mission-armed-green.svg)](https://github.com/nagyesta/abort-mission)

# Lowkey Vault - Docker

This is the root of the Docker image. Visit the [Readme](../README.md) in the repo root for more information about the project in general.

## ARM builds

> [!TIP]
> Lowkey Vault offers a multi-arch image variant too. You can find the relevant project [here](https://github.com/nagyesta/lowkey-vault-docker-buildx).

## Startup parameters

In case you need to change any of the default parameters, you can use the `LOWKEY_ARGS` environment variable with
all necessary arguments supported by [Lowkey Vault App](../lowkey-vault-app/README.md). [Gradle example](build.gradle.kts#L63).

Shell example:

```shell
export LOWKEY_ARGS="--server.port=8444 --LOWKEY_DEBUG_REQUEST_LOG=true"
docker run --rm --name lowkey -e LOWKEY_ARGS -d -p 8444:8444 nagyesta/lowkey-vault:<version>
```

### Custom port use

When you are using Docker, it might not be enough to provide the `--server.port=<port>` argument
as you have a container port and host port. In this situation, you will need to either make sure to
match the two (as the example shows below), or you need to start using the logical vault URIs and the
"proxy" feature of the [Lowkey Vault Client](../lowkey-vault-client/README.md).

```shell
export LOWKEY_ARGS="--server.port=8444"
docker run --rm --name lowkey -e LOWKEY_ARGS -d -p 8444:8444 nagyesta/lowkey-vault:<version>
```

### Relaxed port matching

If you want to use Lowkey Vault in a scenario where you are accessing the vault through a dynamically mapped port,
for example using a random host port when exposing your container port with Testcontainers, you can tell Lowkey Vault
to ignore the port number when searching for a vault based on the request authority (essentially only matching based
on the request's hostname). To activate this feature, you need to use `v2.7.0` or higher, and provide the
`--LOWKEY_VAULT_RELAXED_PORTS=true` argument during startup:

```shell
export LOWKEY_ARGS="--LOWKEY_VAULT_RELAXED_PORTS=true"
docker run --rm --name lowkey -e LOWKEY_ARGS -d -p 8443 nagyesta/lowkey-vault:<version>
```

### Using simulated Managed Identity

In case you want to rely on the built-in simulated Managed Identity token endpoint, you must make sure
to forward the relevant `8080` HTTP only port as well. The host port you are using can be anything you
would like to use in this case. This will make the `/metadata/identity/oauth2/token` endpoint available.
Please check the [example projects](../README.md#example-projects) to see how you can use the provided
endpoint.

```shell
docker run --rm --name lowkey -d -p 8080:8080 -p 8444:8444 nagyesta/lowkey-vault:<version>
```

### Importing vault content at startup

When you need to automatically import the contents of the vaults form a previously created JSON export, you can
use a few parameters to customise the way import is happening. It is recommended to use the `/import` folder to mount
a volume to be able to read the exported content from the host machine.

| Property                      | Default         | Description                                                                                                  |
| ----------------------------- | --------------- | ------------------------------------------------------------------------------------------------------------ |
| `LOWKEY_IMPORT_LOCATION`      | `<null>`        | The JSON file we want to import. If left on default, or the file cannot be read, the import will not happen. |
| `LOWKEY_IMPORT_TEMPLATE_HOST` | `localhost`     | The host name we want to use for replacing `{{host}}` placeholders in the JSON export (if there are any).    |
| `LOWKEY_IMPORT_TEMPLATE_PORT` | `<server.port>` | The port number we want to use for replacing `{{port}}` placeholders in the JSON export (if there are any).  |

Similarly to how `LOWKEY_IMPORT_TEMPLATE_HOST` and `LOWKEY_IMPORT_TEMPLATE_PORT` are replacing placeholders,
we can use `{{now <seconds>}}` placeholders (with positive or negative values as well) which will be replaced
with the `<UTC_Epoch_seconds_now> + <seconds>` formula. This can allow you to use relative timestamps, in case
your tests need a key of certain age relative to the time of the test execution.

> [!TIP]
> Since we want to import vault content, in order to avoid collisions, it is recommended to disable automatic vault registration when using the import feature by adding the `--LOWKEY_VAULT_NAMES=-` argument.

Example:

```shell
# Create volumes, allowing us to share the backup between the container and the host
docker volume create -d local -o type=none -o o=bind -o device=$PWD/import docker-lv-import

export LOWKEY_ARGS="--LOWKEY_VAULT_NAMES=- --LOWKEY_IMPORT_LOCATION=/import/export.json --LOWKEY_IMPORT_TEMPLATE_HOST=127.0.0.1 --LOWKEY_IMPORT_TEMPLATE_PORT=443"
docker run --rm --name lowkey -e LOWKEY_ARGS -v docker-lv-import:/import/:rw -d -p 8443 nagyesta/lowkey-vault:<version>
```

### Exporting vault content on change

There are some use-cases, where it is important to be able to export the content of the vault automatically after each change.
In these cases, the export feature can be turned on by defining the file where the content should be written.
This feature is available using `v3.1.0` or higher. It is recommended to use the `/import` folder to mount a volume with read 
and write mode to be able to persist the exported content on the host machine.

> [!NOTE]
> The exports are not using any placeholders, it is recommended to always use the same host AND either always use the same port or turn on relaxed port matching.

> [!WARNING]
> This feature is not active by default because it can degrade the performance of Lowkey Vault significantly.

> [!TIP]
> Combining this feature with the import feature and using the same file as import source and export target can help you make Lowkey Vault persistent, allowing you to continue from where you have left off the last time.

> [!CAUTION]
> Despite the fact that the persistence can help you use the same vault contents every time when you run Lowkey Vault, please keep in mind, that you should never use Lowkey Vault to store real secrets/keys/certificates because it does absolutely nothing to keep them safe.

Example:

```shell
# Create volumes, allowing us to share the backup between the container and the host
docker volume create -d local -o type=none -o o=bind -o device=$PWD/import docker-lv-import

export LOWKEY_ARGS="--LOWKEY_EXPORT_LOCATION=/import/export.json"
docker run --rm --name lowkey -e LOWKEY_ARGS -v docker-lv-import:/import/:rw -d -p 8443 nagyesta/lowkey-vault:<version>
```

### External configuration

Since Lowkey Vault is a Spring Boot application, the default mechanism for Spring Boot external
configuration can work as well. For example, if there is a `./config/application.properties` file relative
to the folder where the Jar is running, the contents will be picked up automatically. To utilize this, the
recommended option is to attach a `\*.properties` file to `/config/application.properties` (path inside the
container) using a volume.
