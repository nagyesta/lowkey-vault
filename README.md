![LowkeyVault](.github/assets/LowkeyVault-logo.png)

[![GitHub license](https://img.shields.io/github/license/nagyesta/lowkey-vault?color=informational)](https://raw.githubusercontent.com/nagyesta/lowkey-vault/main/LICENSE)
[![Java version](https://img.shields.io/badge/Java%20version-11-yellow?logo=java)](https://img.shields.io/badge/Java%20version-11-yellow?logo=java)
[![latest-release](https://img.shields.io/github/v/tag/nagyesta/lowkey-vault?color=blue&logo=git&label=releases&sort=semver)](https://github.com/nagyesta/lowkey-vault/releases)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.nagyesta.lowkey-vault.app/lowkey-vault-app?logo=apache-maven)](https://search.maven.org/search?q=com.github.nagyesta.lowkey-vault)
[![Docker Hub](https://img.shields.io/docker/v/nagyesta/lowkey-vault?label=docker%20hub&logo=docker&sort=semver)](https://hub.docker.com/repository/docker/nagyesta/lowkey-vault)
[![JavaCI](https://img.shields.io/github/workflow/status/nagyesta/lowkey-vault/JavaCI?logo=github)](https://img.shields.io/github/workflow/status/nagyesta/lowkey-vault/JavaCI?logo=github)

[![code-climate-maintainability](https://img.shields.io/codeclimate/maintainability/nagyesta/lowkey-vault?logo=code%20climate)](https://img.shields.io/codeclimate/maintainability/nagyesta/lowkey-vault?logo=code%20climate)
[![code-climate-tech-debt](https://img.shields.io/codeclimate/tech-debt/nagyesta/lowkey-vault?logo=code%20climate)](https://img.shields.io/codeclimate/tech-debt/nagyesta/lowkey-vault?logo=code%20climate)
[![last_commit](https://img.shields.io/github/last-commit/nagyesta/lowkey-vault?logo=git)](https://img.shields.io/github/last-commit/nagyesta/lowkey-vault?logo=git)
[![badge-abort-mission-armed-green](https://raw.githubusercontent.com/nagyesta/abort-mission/wiki_assets/.github/assets/badge-abort-mission-armed-green.svg)](https://github.com/nagyesta/abort-mission)

Lowkey Vault is a stub aspiring to be compatible with [Azure Key Vault](https://azure.microsoft.com/en-us/services/key-vault/) REST APIs.
The project aims to provide a low footprint alternative for the cases when using a real Key Vault is not practical or impossible.

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
4. Initialize your keys using the client
5. Run your code
6. Stop Lowkey Vault

### Docker

1. Pull the most recent version from ```nagyesta/lowkey-vault```
2. ```docker run lowkey-vault:0.1.0 -p 8443:8443```
3. Use ```https://localhost:8443``` as key vault URI when using
   the [Azure Key Vault Key client](https://docs.microsoft.com/en-us/azure/key-vault/keys/quick-create-java)
4. Initialize your keys using the client
5. Run your code
6. Stop Lowkey Vault

## Features

Lowkey Vault is far from supporting all Azure Key Vault features. The list supported functionality can be found here:

### Keys

- API version supported: ```7.2```
- Create key (```RSA```, ```EC```, ```OCT```)
    - Including metadata
- Get available key versions
- Get key
    - Latest
    - Specific version

### Limitations

Although basic support for different vaults is baked in, only a handful of default vaults are available based on hostname at the moment.
These are configured [here](lowkey-vault-app/src/main/java/com/github/nagyesta/lowkeyvault/AppConfiguration.java).
