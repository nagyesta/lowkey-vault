# Security Policy

## Err on the safer side

As Lowkey Vault is intended to be a standalone test double allowing you to use it when testing with an Azure Key Vault would be complicated
or impossible. It is **strongly recommended to never deploy the Lowkey Vault artifacts** together with your production service/product.
Although this is not ensuring that it won't be ever a source you would need to consider as a source of risk, it would certainly make it a
bit harder to use for malicious actors. When deploying your Lowkey Vault App instance, please make sure to secure your infrastructure
properly.

## Supported Versions

The aim is to support fellow developers as much as possible with security updates, this is a security focused project after all. At the end
of the day, this is a hobby project which is maintained in my free time. So reality is that the latest version will be supported with security
patches in case vulnerabilities are reported and everything else will be decided case by case.

[![Supported version](https://img.shields.io/github/v/tag/nagyesta/lowkey-vault?color=green&logo=git&label=Supported%20version&sort=semver)](https://img.shields.io/github/v/tag/nagyesta/lowkey-vault?color=green&logo=git&label=Supported%20version&sort=semver)

## Reporting a Vulnerability

In case you have found a vulnerability, please report an [issue here](https://github.com/nagyesta/lowkey-vault/issues)

Thank you in advance!

## Vulnerability Response

Once a vulnerability is reported, I will try to fix it as soon as I can afford the time, preferably under less than 60 days from receiving a
valid security vulnerability report.

In case of vulnerable dependencies, response time depends on the release of the known safe/fixed dependency version as well. As long as
there is no such available version, the update activity is considered to be blocked, therefore the normal response timeline does not apply.
