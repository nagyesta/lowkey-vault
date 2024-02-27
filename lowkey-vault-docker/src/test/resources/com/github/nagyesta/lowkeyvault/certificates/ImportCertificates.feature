Feature: Certificate import

    @Certificate @CertificateImport @RSA
    Scenario Outline: RSA_CERT_IMPORT_01 Single versions of RSA certificates can be imported with the certificate client
        Given certificate API version <api> is used
        And a certificate client is created with the vault named certs-generic
        When a certificate named <certName> is imported from the resource named <fileName> using <password> as password
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
            | 7.4 | 74-importRsaCert2048Pem  | rsa-localhost.pem   | -        | PEM    | CN=localhost   | 2052-08-28 |
            | 7.5 | 75-importRsaCert2048Pem  | rsa-localhost.pem   | -        | PEM    | CN=localhost   | 2052-08-28 |

    @Certificate @CertificateImport @EC
    Scenario Outline: EC_CERT_IMPORT_01 Single versions of EC certificates can be imported with the certificate client
        Given certificate API version <api> is used
        And a certificate client is created with the vault named certs-generic
        When a certificate named <certName> is imported from the resource named <fileName> using <password> as password
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
            | 7.4 | 74-importEc521Pem  | ec521-ec-localhost.pem | -        | PEM    | CN=ec.localhost | 2023-09-10 |
            | 7.5 | 75-importEc521Pem  | ec521-ec-localhost.pem | -        | PEM    | CN=ec.localhost | 2023-09-10 |

    @Certificate @CertificateImport @CertificateUpdate @RSA
    Scenario Outline: RSA_CERT_UPDATE_01 Imported RSA certificates can be updated with the certificate client
        Given certificate API version <api> is used
        And a certificate client is created with the vault named certs-generic
        And a certificate named <certName> is imported from the resource named <fileName> using <password> as password
        And the certificate has no tags
        And the certificate policy named <certName> is downloaded
        And the downloaded certificate policy has <subject> as subject
        And the downloaded certificate policy has <type> as type
        When the certificate named <certName> is updated to have <updatedSubject> as subject and <updatedType> as type
        And the certificate named <certName> is updated to contain the tag named updated with true as value
        Then the certificate has a tag named updated with true as value
        And the certificate policy named <certName> is downloaded
        And the downloaded certificate policy has <updatedSubject> as subject
        And the downloaded certificate policy has <updatedType> as type

        Examples:
            | api | certName                 | fileName            | password | type   | updatedType | subject        | updatedSubject   |
            | 7.3 | 73-updateRsaCert2048Pem  | rsa-localhost.pem   | -        | PEM    | PKCS12      | CN=localhost   | CN=localhost     |
            | 7.3 | 73-updateRsaCert2048Pkcs | rsa-localhost.p12   | changeit | PKCS12 | PKCS12      | CN=localhost   | CN=updated.local |
            | 7.3 | 73-updateRsaCert4096Pem  | rsa-example-com.pem | -        | PEM    | PEM         | CN=example.com | CN=updated.local |
            | 7.3 | 73-updateRsaCert4096Pkcs | rsa-example-com.p12 | password | PKCS12 | PEM         | CN=example.com | CN=example.com   |
            | 7.4 | 74-updateRsaCert2048Pkcs | rsa-localhost.p12   | changeit | PKCS12 | PKCS12      | CN=localhost   | CN=updated.local |
            | 7.5 | 75-updateRsaCert2048Pkcs | rsa-localhost.p12   | changeit | PKCS12 | PKCS12      | CN=localhost   | CN=updated.local |

    @Certificate @CertificateImport @CertificateUpdate @EC
    Scenario Outline: EC_CERT_UPDATE_01 Imported EC certificates can be updated with the certificate client
        Given certificate API version <api> is used
        And a certificate client is created with the vault named certs-generic
        And a certificate named <certName> is imported from the resource named <fileName> using <password> as password
        And the certificate has no tags
        And the certificate policy named <certName> is downloaded
        And the downloaded certificate policy has <subject> as subject
        And the downloaded certificate policy has <type> as type
        When the certificate named <certName> is updated to have <updatedSubject> as subject and <updatedType> as type
        And the certificate named <certName> is updated to contain the tag named updated with true as value
        Then the certificate has a tag named updated with true as value
        And the certificate policy named <certName> is downloaded
        And the downloaded certificate policy has <updatedSubject> as subject
        And the downloaded certificate policy has <updatedType> as type

        Examples:
            | api | certName           | fileName               | password | type   | updatedType | subject         | updatedSubject   |
            | 7.3 | 73-updateEc521Pem  | ec521-ec-localhost.pem | -        | PEM    | PKCS12      | CN=ec.localhost | CN=updated.local |
            | 7.3 | 73-updateEc521Pkcs | ec521-ec-localhost.p12 | changeit | PKCS12 | PEM         | CN=ec.localhost | CN=ec.localhost  |
            | 7.4 | 74-updateEc521Pkcs | ec521-ec-localhost.p12 | changeit | PKCS12 | PEM         | CN=ec.localhost | CN=ec.localhost  |
            | 7.5 | 75-updateEc521Pkcs | ec521-ec-localhost.p12 | changeit | PKCS12 | PEM         | CN=ec.localhost | CN=ec.localhost  |
