![LowkeyVault](.github/assets/LowkeyVault-logo-full.png)

[![GitHub license](https://img.shields.io/github/license/nagyesta/lowkey-vault?color=informational)](https://raw.githubusercontent.com/nagyesta/lowkey-vault/main/LICENSE)
[![Java version](https://img.shields.io/badge/Java%20version-11-yellow?logo=java)](https://img.shields.io/badge/Java%20version-11-yellow?logo=java)
[![latest-release](https://img.shields.io/github/v/tag/nagyesta/lowkey-vault?color=blue&logo=git&label=releases&sort=semver)](https://github.com/nagyesta/lowkey-vault/releases)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.nagyesta.lowkey-vault/lowkey-vault-app?logo=apache-maven)](https://search.maven.org/search?q=com.github.nagyesta.lowkey-vault)
[![Docker Hub](https://img.shields.io/docker/v/nagyesta/lowkey-vault?label=docker%20hub&logo=docker&sort=semver)](https://hub.docker.com/r/nagyesta/lowkey-vault)
[![JavaCI](https://img.shields.io/github/actions/workflow/status/nagyesta/lowkey-vault/gradle.yml?logo=github&branch=main)](https://github.com/nagyesta/lowkey-vault/actions/workflows/gradle.yml)

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

> Lowkey Vault is NOT intended as an [Azure Key Vault](https://azure.microsoft.com/en-us/services/key-vault/) replacement. Please do not attempt using it instead of the real service in production as it is not using any security measures to keep your secrets safe.

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

Note: a complex example is available [here](https://github.com/nagyesta/lowkey-vault-example-docker)

1. Pull the most recent version from ```nagyesta/lowkey-vault```
   - You can find a list of all the available tags [here](https://hub.docker.com/r/nagyesta/lowkey-vault/tags)
2. ```docker run --rm  -p 8443:8443 nagyesta/lowkey-vault:<version>```
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

- API version supported: ```7.2```, partially ```7.3```
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
- Backup and restore keys
- Get random bytes
- Rotate keys
  - Manually
  - Automatically when time-shift is used with an applicable rotation policy
- Get rotation policy
- Update rotation policy

### Secrets

- API version supported: ```7.2```, ```7.3```
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
- Backup and restore secrets

### Certificates

![https://img.shields.io/badge/status-experimental-red](https://img.shields.io/badge/status-experimental-red)

- API version supported: ```7.3```
- Create certificate
    - Self-signed only
    - Using `PKCS12` (`.pfx`) or `PEM` (`.pem`) formats
    - The downloadable certificate is protected using a blank (`""`) password for `PKCS12` stores
- Get certificate operation
    - Get pending create operation results
    - Get pending delete operation results
- Get available certificate versions
- Get certificate
    - Latest version of a single certificate
    - Specific version of a single certificate
    - List of all certificates
- Get certificate policy
- Import certificate
    - Self-signed only
    - Using `PKCS12` (`.pfx`) or `PEM` (`.pem`) formats
    - The downloadable certificate is protected using a blank (`""`) password for `PKCS12` stores
- Get deleted certificate
    - Latest version of a single certificate
    - List of all certificate
- Delete certificate
- Recover deleted certificate
- Purge deleted certificate

#### Warning!

Certificate API features are work in progress, many Lowkey Vault features might 
not work or are known to be broken, for example but not limited to the following:

- Certificate lifetime policy does not have any effect
- Import and export ignores certificates
- Time shift is not supported for certificates

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
- Export vault contents (to be able to import it at startup later)

#### Swagger

[https://localhost:8443/api/swagger-ui/index.html](https://localhost:8443/api/swagger-ui/index.html)

## Startup parameters

1. Using the `.jar`: [Lowkey Vault App](lowkey-vault-app/README.md).
2. Using Docker: [Lowkey Vault Docker](lowkey-vault-docker/README.md).
3. Using Testcontainers: [Lowkey Vault Testcontainers](lowkey-vault-testcontainers/README.md).

# Example projects

1. [Java](https://github.com/nagyesta/lowkey-vault-example)
2. [.Net](https://github.com/nagyesta/lowkey-vault-example-dotnet)
3. [Python](https://github.com/nagyesta/lowkey-vault-example-python)
4. [Go](https://github.com/nagyesta/lowkey-vault-example-go)
5. [Node.js](https://github.com/nagyesta/lowkey-vault-example-nodejs)
6. [Docker](https://github.com/nagyesta/lowkey-vault-example-docker)

# Limitations

- Some encryption/signature algorithms are not supported. Please refer to the ["Features"](#features) section for the up-to-date list of supported algorithms.
- Certificate Vault features are not supported at the moment
- Recovery options cannot be configured for vaults created during start-up
