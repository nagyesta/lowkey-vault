Feature: Get certificate policy

  @Certificate @CertificateImport @CertificateGetPolicy @RSA
  Scenario Outline: RSA_CERT_GET_POLICY_01 Policy data of RSA certificates can be accessed with the certificate client
    Given certificate API version <api> is used
    And a certificate client is created with the vault named certs-generic
    And a certificate named <certName> is imported from the resource named <fileName> using <password> as password
    When the certificate policy named <certName> is downloaded
    Then the downloaded certificate policy has <validity> months validity
    And the downloaded certificate policy has <subject> as subject

    Examples:
      | api | certName                | fileName            | password | subject        | validity |
      | 7.3 | 73-policyRsaCert2048Pem | rsa-localhost.pem   | -        | CN=localhost   | 360      |
      | 7.3 | 73-policyRsaCert4096Pem | rsa-example-com.pem | -        | CN=example.com | 12       |

  @Certificate @CertificateImport @CertificateGetPolicy @EC
  Scenario Outline: EC_CERT_GET_POLICY_01 Policy data of EC certificates can be accessed with the certificate client
    Given certificate API version <api> is used
    And a certificate client is created with the vault named certs-generic
    And a certificate named <certName> is imported from the resource named <fileName> using <password> as password
    When the certificate policy named <certName> is downloaded
    Then the downloaded certificate policy has <validity> months validity
    And the downloaded certificate policy has <subject> as subject

    Examples:
      | api | certName          | fileName               | password | subject         | validity |
      | 7.3 | 73-policyEc521Pem | ec521-ec-localhost.pem | -        | CN=ec.localhost | 12       |
