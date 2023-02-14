![LowkeyVault](../.github/assets/LowkeyVault-logo-full.png)

[![GitHub license](https://img.shields.io/github/license/nagyesta/lowkey-vault?color=informational)](https://raw.githubusercontent.com/nagyesta/lowkey-vault/main/LICENSE)
[![Java version](https://img.shields.io/badge/Java%20version-11-yellow?logo=java)](https://img.shields.io/badge/Java%20version-11-yellow?logo=java)
[![latest-release](https://img.shields.io/github/v/tag/nagyesta/lowkey-vault?color=blue&logo=git&label=releases&sort=semver)](https://github.com/nagyesta/lowkey-vault/releases)
[![Docker Hub](https://img.shields.io/docker/v/nagyesta/lowkey-vault?label=docker%20hub&logo=docker&sort=semver)](https://hub.docker.com/repository/docker/nagyesta/lowkey-vault)
[![Docker Pulls](https://img.shields.io/docker/pulls/nagyesta/lowkey-vault?logo=docker)](https://hub.docker.com/repository/docker/nagyesta/lowkey-vault)
[![JavaCI](https://img.shields.io/github/actions/workflow/status/nagyesta/lowkey-vault/gradle.yml?logo=github&branch=main)](https://github.com/nagyesta/lowkey-vault/actions/workflows/gradle.yml)
[![badge-abort-mission-armed-green](https://raw.githubusercontent.com/nagyesta/abort-mission/wiki_assets/.github/assets/badge-abort-mission-armed-green.svg)](https://github.com/nagyesta/abort-mission)

# Lowkey Vault - Docker

This is the root of the Docker image. Visit the [Readme](../README.md) in the repo root for more information about the project in general.

## Startup parameters

In case you need to change any of the default parameters, you can use the ```LOWKEY_ARGS``` environment variable with
all necessary arguments supported by [Lowkey Vault App](../lowkey-vault-app/README.md). [Gradle example](build.gradle#L63).

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

## ARM builds

Lowkey Vault offers a multi-arch variant using Buildx. You can find the relevant project [here](https://github.com/nagyesta/lowkey-vault-docker-buildx).
