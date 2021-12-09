# Security Policy

## Err on the safer side

As Lowkey Vault is intended to be a standalone test double allowing you to use it when testing with an Azure Key Vault would be complicated
or impossible. It is **strongly recommended to never deploy the Lowkey Vault artifacts** together with your production service/product.
Although this is not ensuring that it won't be ever a source you would need to consider as a source of risk, it would certainly make it a
bit harder to use for malicious actors. When deploying your Lowkey Vault App instance, please make sure to secure your infrastructure
properly.

## Supported Versions

The aim is to support fellow developers as much as possible with security updates, this is a security focused project after all. At the end
of the day, this is a hobby project which maintained in my free time. So reality is that the latest version will be supported with security
patches in case vulnerabilities are reported and everything else will be decided case by case.

[![Supported version](https://img.shields.io/github/v/tag/nagyesta/lowkey-vault?color=green&logo=git&label=Supported%20version&sort=semver)](https://img.shields.io/github/v/tag/nagyesta/lowkey-vault?color=green&logo=git&label=Supported%20version&sort=semver)

## Reporting a Vulnerability

In case you have found a vulnerability, please report an [issue here](https://github.com/nagyesta/lowkey-vault/issues)

Thank you in advance!
