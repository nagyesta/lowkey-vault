![LowkeyVault](../.github/assets/LowkeyVault-logo-full.png)

[![GitHub license](https://img.shields.io/github/license/nagyesta/lowkey-vault?color=informational)](https://raw.githubusercontent.com/nagyesta/lowkey-vault/main/LICENSE)
[![Java version](https://img.shields.io/badge/Java%20version-17-yellow?logo=java)](https://img.shields.io/badge/Java%20version-17-yellow?logo=java)
[![latest-release](https://img.shields.io/github/v/tag/nagyesta/lowkey-vault?color=blue&logo=git&label=releases&sort=semver)](https://github.com/nagyesta/lowkey-vault/releases)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.nagyesta.lowkey-vault/lowkey-vault-app?logo=apache-maven)](https://search.maven.org/search?q=com.github.nagyesta.lowkey-vault)

[![JavaCI](https://img.shields.io/github/actions/workflow/status/nagyesta/lowkey-vault/gradle.yml?logo=github&branch=main)](https://github.com/nagyesta/lowkey-vault/actions/workflows/gradle.yml)
[![Sonar Coverage](https://img.shields.io/sonar/coverage/nagyesta_lowkey-vault?server=https%3A%2F%2Fsonarcloud.io&logo=sonarcloud&logoColor=white)](https://sonarcloud.io/summary/new_code?id=nagyesta_lowkey-vault)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=nagyesta_lowkey-vault&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=nagyesta_lowkey-vault)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=nagyesta_lowkey-vault&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=nagyesta_lowkey-vault)
[![badge-abort-mission-armed-green](https://raw.githubusercontent.com/nagyesta/abort-mission/wiki_assets/.github/assets/badge-abort-mission-armed-green.svg)](https://github.com/nagyesta/abort-mission)

# Lowkey Vault - Client

This is the root of the Java HTTP Client Provider that is solving some certificate and base URI issues in our tests.
Visit the [Readme](../README.md) in the repo root for more information about the project in general.

## Usage

1. Either download manually the .jar from the packages or
   use [Maven Central](https://search.maven.org/search?q=com.github.nagyesta.lowkey-vault).
2. Create a [ApacheHttpClientProvider](src/main/java/com/github/nagyesta/lowkeyvault/http/ApacheHttpClientProvider.java) instance using the
   constructor supporting host overrides: `ApacheHttpClientProvider(final String vaultUrl, final Function<URI, URI> authorityOverrideFunction)`.
   [Example](../lowkey-vault-docker/src/test/java/com/github/nagyesta/lowkeyvault/steps/SecretsStepDefs.java#L32-35)
3. Use your `ApacheHttpClientProvider` to get your key/secret/crypto clients.
4. Done.

## Note

In case you don't wish to use this provider, it is perfectly fine. Make sure to use the same steps to mimic what it does:

1. Set any basic credentials (Lowkey Vault will check whether they are there but ignore the value.)
2. Route your requests to the port used by your Lowkey Vault instance
3. Make sure that the host headers or the actual URLs used by your client contain the host and port expected by Lowkey Vault
4. Accept the self-signed certificate used by Lowkey-Vault in your HTTP client.
