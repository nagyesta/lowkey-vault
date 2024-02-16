Feature: Get certificate policy

    @Certificate @CertificateImport @CertificateGetPolicy @RSA
    Scenario Outline: RSA_CERT_GET_POLICY_01 Policy data of RSA certificates can be accessed with the certificate client
        Given certificate API version <api> is used
        And a certificate client is created with the vault named certs-generic
        And a certificate named <certName> is imported from the resource named <fileName> covering <subject> using a lifetime action
        When the certificate policy named <certName> is downloaded
        Then the downloaded certificate policy has <validity> months validity
        And the downloaded certificate policy has keyEncipherment and digitalSignature as key usages
        And the downloaded certificate policy has 1.3.6.1.5.5.7.3.1 and 1.3.6.1.5.5.7.3.2 as enhanced key usages
        And the downloaded certificate policy has <subject> as subject
        And the lifetime action triggers EmailContacts when 80 percent lifetime reached

        Examples:
            | api | certName                | fileName            | subject        | validity |
            | 7.3 | 73-policyRsaCert2048Pem | rsa-localhost.pem   | CN=localhost   | 360      |
            | 7.3 | 73-policyRsaCert4096Pem | rsa-example-com.pem | CN=example.com | 12       |
            | 7.4 | 74-policyRsaCert2048Pem | rsa-localhost.pem   | CN=localhost   | 360      |
            | 7.5 | 75-policyRsaCert2048Pem | rsa-localhost.pem   | CN=localhost   | 360      |

    @Certificate @CertificateImport @CertificateGetPolicy @EC
    Scenario Outline: EC_CERT_GET_POLICY_01 Policy data of EC certificates can be accessed with the certificate client
        Given certificate API version <api> is used
        And a certificate client is created with the vault named certs-generic
        And a certificate named <certName> is imported from the resource named <fileName> covering <subject> using a lifetime action
        When the certificate policy named <certName> is downloaded
        Then the downloaded certificate policy has <validity> months validity
        And the downloaded certificate policy has <subject> as subject
        And the lifetime action triggers EmailContacts when 80 percent lifetime reached

        Examples:
            | api | certName          | fileName               | subject         | validity |
            | 7.3 | 73-policyEc521Pem | ec521-ec-localhost.pem | CN=ec.localhost | 12       |
            | 7.4 | 74-policyEc521Pem | ec521-ec-localhost.pem | CN=ec.localhost | 12       |
            | 7.5 | 75-policyEc521Pem | ec521-ec-localhost.pem | CN=ec.localhost | 12       |
