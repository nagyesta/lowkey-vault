Feature: Certificate backup and restore

    @Certificate @CertificateImport @CertificateBackup @CertificateRestore @RSA
    Scenario Outline: RSA_CERT_BACKUP_01 Single versions of RSA certificates can be backed up and restored with the certificate client
        Given certificate API version <api> is used
        And a vault is created with name certs-backup-<certName>
        And a certificate client is created with the vault named certs-backup-<certName>
        And a certificate named <certName> is imported from the resource named <fileName> using <password> as password
        And the certificate named <certName> is backed up
        And the vault named certs-backup-<certName> is deleted
        And the vault named certs-backup-<certName> is purged
        And a vault is created with name certs-backup-<certName>
        When the certificate named <certName> is restored
        Then the certificate is enabled
        And the certificate secret named <certName> is downloaded
        And the downloaded secret contains a <type> certificate
        And the downloaded <type> certificate store expires on <expiry>
        And the downloaded <type> certificate store has a certificate with <subject> as subject
        And the downloaded <type> certificate store content matches store from <fileName> using <password> as password
        And the lifetime action triggers AutoRenew when 80 percent lifetime reached

        Examples:
            | api | certName                 | fileName            | password | type   | subject        | expiry     |
            | 7.3 | 73-importRsaCert2048Pem  | rsa-localhost.pem   | -        | PEM    | CN=localhost   | 2052-08-28 |
            | 7.3 | 73-importRsaCert2048Pkcs | rsa-localhost.p12   | changeit | PKCS12 | CN=localhost   | 2052-08-28 |
            | 7.3 | 73-importRsaCert4096Pem  | rsa-example-com.pem | -        | PEM    | CN=example.com | 2024-01-27 |
            | 7.3 | 73-importRsaCert4096Pkcs | rsa-example-com.p12 | password | PKCS12 | CN=example.com | 2024-01-27 |

    @Certificate @CertificateImport @CertificateBackup @CertificateRestore @EC
    Scenario Outline: EC_CERT_BACKUP_01 Single versions of EC certificates can be backed up and restored with the certificate client
        Given certificate API version <api> is used
        And a vault is created with name certs-backup-<certName>
        And a certificate client is created with the vault named certs-backup-<certName>
        And a certificate named <certName> is imported from the resource named <fileName> using <password> as password
        And the certificate named <certName> is backed up
        And the vault named certs-backup-<certName> is deleted
        And the vault named certs-backup-<certName> is purged
        And a vault is created with name certs-backup-<certName>
        When the certificate named <certName> is restored
        Then the certificate is enabled
        And the certificate secret named <certName> is downloaded
        And the downloaded secret contains a <type> certificate
        And the downloaded <type> certificate store expires on <expiry>
        And the downloaded <type> certificate store has a certificate with <subject> as subject
        And the downloaded <type> certificate store content matches store from <fileName> using <password> as password
        And the lifetime action triggers AutoRenew when 80 percent lifetime reached

        Examples:
            | api | certName           | fileName               | password | type   | subject         | expiry     |
            | 7.3 | 73-importEc521Pem  | ec521-ec-localhost.pem | -        | PEM    | CN=ec.localhost | 2023-09-10 |
            | 7.3 | 73-importEc521Pkcs | ec521-ec-localhost.p12 | changeit | PKCS12 | CN=ec.localhost | 2023-09-10 |
