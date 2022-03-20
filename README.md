![LowkeyVault](.github/assets/LowkeyVault-logo-full.png)

[![GitHub license](https://img.shields.io/github/license/nagyesta/lowkey-vault?color=informational)](https://raw.githubusercontent.com/nagyesta/lowkey-vault/main/LICENSE)
[![Java version](https://img.shields.io/badge/Java%20version-11-yellow?logo=java)](https://img.shields.io/badge/Java%20version-11-yellow?logo=java)
[![latest-release](https://img.shields.io/github/v/tag/nagyesta/lowkey-vault?color=blue&logo=git&label=releases&sort=semver)](https://github.com/nagyesta/lowkey-vault/releases)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.nagyesta.lowkey-vault/lowkey-vault-app?logo=apache-maven)](https://search.maven.org/search?q=com.github.nagyesta.lowkey-vault)
[![Docker Hub](https://img.shields.io/docker/v/nagyesta/lowkey-vault?label=docker%20hub&logo=docker&sort=semver)](https://hub.docker.com/repository/docker/nagyesta/lowkey-vault)
[![JavaCI](https://img.shields.io/github/workflow/status/nagyesta/lowkey-vault/JavaCI?logo=github)](https://img.shields.io/github/workflow/status/nagyesta/lowkey-vault/JavaCI?logo=github)

[![CII Best Practices](https://bestpractices.coreinfrastructure.org/projects/5577/badge)](https://bestpractices.coreinfrastructure.org/projects/5577)
[![code-climate-maintainability](https://img.shields.io/codeclimate/maintainability/nagyesta/lowkey-vault?logo=code%20climate)](https://img.shields.io/codeclimate/maintainability/nagyesta/lowkey-vault?logo=code%20climate)
[![code-climate-tech-debt](https://img.shields.io/codeclimate/tech-debt/nagyesta/lowkey-vault?logo=code%20climate)](https://img.shields.io/codeclimate/tech-debt/nagyesta/lowkey-vault?logo=code%20climate)
[![last_commit](https://img.shields.io/github/last-commit/nagyesta/lowkey-vault?logo=git)](https://img.shields.io/github/last-commit/nagyesta/lowkey-vault?logo=git)
[![badge-abort-mission-armed-green](https://raw.githubusercontent.com/nagyesta/abort-mission/wiki_assets/.github/assets/badge-abort-mission-armed-green.svg)](https://github.com/nagyesta/abort-mission)

Lowkey Vault is a test double (fake object) aspiring to be compatible
with [Azure Key Vault](https://azure.microsoft.com/en-us/services/key-vault/) REST APIs. The project aims to provide a low footprint
alternative for the cases when using a real Key Vault is not practical or impossible.

## Recommended use

### Warning!

> Lowkey Vault is NOT intended as a [Azure Key Vault](https://azure.microsoft.com/en-us/services/key-vault/) replacement. Please do not attempt using it instead of the real service in production as it is not using any security measures to keep your secrets safe.

### Valid use-cases

I have an app using Azure Key Vault and:

- I want to be able to run my tests locally without internet connection; or
- I do not want to keep a Key Vault alive for my CI instances; or
- I do not want to figure out how to provide a new Key Vault every time my test run; or
- I do not want to worry about authentication when using Key Vault locally.

## Quick start guide

### Java

1. Either download manually the Spring Boot app from the packages or
   use [Maven Central](https://search.maven.org/search?q=com.github.nagyesta.lowkey-vault).
2. Start Lowkey Vault jar
3. Use ```https://localhost:8443``` as key vault URI when using
   the [Azure Key Vault Key client](https://docs.microsoft.com/en-us/azure/key-vault/keys/quick-create-java)
   or the [Azure Key Vault Secret client](https://docs.microsoft.com/en-us/azure/key-vault/secrets/quick-create-java)
   and set any basic credentials (Lowkey Vault will check whether they are there but ignore the value.)
4. If you are using more than one vaults parallel
    1. Either set up all of their host names in hosts to point to localhost
    2. Or, use the provider in [lowkey-vault-client](lowkey-vault-client/README.md) to handle the mapping for you
    3. (Or mimic the same using your HTTP client provider)
5. Initialize your keys or secrets using the client
6. Run your code
7. Stop Lowkey Vault

### Docker

1. Pull the most recent version from ```nagyesta/lowkey-vault```
2. ```docker run lowkey-vault:<version> -p 8443:8443```
3. Use ```https://localhost:8443``` as key vault URI when using
   the [Azure Key Vault Key client](https://docs.microsoft.com/en-us/azure/key-vault/keys/quick-create-java)
   or the [Azure Key Vault Secret client](https://docs.microsoft.com/en-us/azure/key-vault/secrets/quick-create-java)
   and set any basic credentials (Lowkey Vault will check whether they are there but ignore the value.)
4. If you are using more than one vaults parallel
    1. Either set up all of their host names in hosts to point to localhost
    2. Or, use the provider in [lowkey-vault-client](lowkey-vault-client/README.md) to handle the mapping for you
    3. (Or mimic the same using your HTTP client provider)
5. Initialize your keys or secrets using the client
6. Run your code
7. Stop Lowkey Vault

## Testcontainers

See examples under [Lowkey Vault Testcontainers](lowkey-vault-testcontainers/README.md).

## Features

Lowkey Vault is far from supporting all Azure Key Vault features. The list supported functionality can be found here:

### Keys

- API version supported: ```7.2```
- Create key (```RSA```, ```EC```, ```OCT```)
    - Including metadata
- Import key  (```RSA```, ```EC```, ```OCT```)
    - Including metadata
- Get available key versions
- Get key
    - Latest version of a single key
    - Specific version of a single key
    - List of all keys
- Get deleted key
    - Latest version of a single key
    - List of all keys
- Delete key
- Update key
- Recover deleted key
- Purge deleted key
- Encrypt/Decrypt/Wrap/Unwrap keys
    - ```RSA``` (```2k```/```3k```/```4k```)
        - ```RSA1_5```
        - ```RSA-OAEP```
        - ```RSA-OAEP-256```
    - ```AES``` (```128```/```192```/```256```)
        - ```AES-CBC```
        - ```AES-CBC Pad```
- Sign/Verify digest with keys
    - ```RSA``` (```2k```/```3k```/```4k```)
        - ```PS256```
        - ```PS384```
        - ```PS512```
        - ```RS256```
        - ```RS384```
        - ```RS512```
    - ```EC``` (```P-256```/```P-256K```/```P-384```/```P-521```)
        - ```ES256```
        - ```ES256K```
        - ```ES384```
        - ```ES512```

### Secrets

- API version supported: ```7.2```
- Set secret
    - Including metadata
- Get available secret versions
- Get secret
    - Latest version of a single secret
    - Specific version of a single secret
    - List of all secrets
- Get deleted secret
    - Latest version of a single secret
    - List of all secrets
- Delete secret
- Update secret
- Recover deleted secret
- Purge deleted secret

### Management API

#### Functionality

- Create vault
- List vaults
- Delete vault
- List deleted vaults
- Recover deleted vault
- Purge vault
- Time-shift (simulate the passing of time)
  - A single vault
  - All vaults

#### Swagger

[https://localhost:8443/api/swagger-ui/index.html](https://localhost:8443/api/swagger-ui/index.html)

## Startup parameters

### Log requests

In order to support debugging integration, Lowkey Vault can log request data. To turn on this feature, 
you must pass ```--LOWKEY_DEBUG_REQUEST_LOG=true``` as startup argument in the
```LOWKEY_ARGS``` env variable when starting the Docker container. [Example](lowkey-vault-docker/build.gradle#L64)

### Non-default vaults

In case you wish to use more than one vaults, you should consider registering additional vaults using
the ```--LOWKEY_VAULT_NAMES=name1,name2``` comma separated format passed in the
```LOWKEY_ARGS``` env variable when starting the Docker container. This will register the ```https://name1.localhost:8443```
and ```https://name2.localhost:8443```
vaults in the aforementioned example. You can pass any number of vault prefixes (as long as you have enough RAM)
. [Example](lowkey-vault-docker/build.gradle#L65)

A handful of default vaults are available by default. These are
configured [here](lowkey-vault-app/src/main/java/com/github/nagyesta/lowkeyvault/AppConfiguration.java).

### Custom port use on host machine

In order to avoid using the reserved `8443` port, we need to tell Lowkey Vault to use a different one instead.
We need to solve different issues depending on the tool we are using.

##### Using the `.jar`

Set `--server.port=<portNumber>` as an argument as usual with Spring Boot apps.

##### Using Docker

In this case the issue is probably just exposing the `8443` port of the container as `8443` when starting it. Adding `-p <portNumber>:8443`
when starting the container should do the trick.
[Example](lowkey-vault-docker/build.gradle#L61)

##### Using Testcontainers

This issue should not happen while using Testcontainers. See examples under [Lowkey Vault Testcontainers](lowkey-vault-testcontainers/README.md).

# Limitations

- Some encryption/signature algorithms are not supported. Please refer to the ["Features"](#features) section for the up-to-date list of supported algorithms.
- Backup and restore features are not supported at the moment
- Certificate Vault features are not supported at the moment
- Recovery options cannot be set as vault creation is implicit during start-up
