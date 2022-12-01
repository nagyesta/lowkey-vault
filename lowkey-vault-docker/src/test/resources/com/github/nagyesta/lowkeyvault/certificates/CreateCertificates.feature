Feature: Certificate creation

  @Certificate @CertificateCreate @RSA
  Scenario Outline: RSA_CERT_CREATE_01 Single versions of RSA certificates can be created with the certificate client
    Given certificate API version <api> is used
    And a certificate client is created with the vault named certs-generic
    And a <type> certificate is prepared with subject <subject>
    And the certificate is set to be <enabledStatus>
    And the certificate is set to use an RSA key with <keySize> and <hsm> HSM
    When the certificate is created with name <certName>
    Then the certificate is <enabledStatus>
    And the certificate secret named <certName> is downloaded
    And the downloaded secret contains a <type> certificate
    And the downloaded <type> certificate store has a certificate with <subject> as subject

    Examples:
      | api | hsm     | certName                    | keySize | enabledStatus | type   | subject        |
      | 7.3 | without | 73-createRsaCert2048Pem     | 2048    | enabled       | PEM    | CN=localhost   |
      | 7.3 | without | 73-createRsaCert4096Pem     | 4096    | enabled       | PEM    | CN=localhost   |
      | 7.3 | without | 73-createRsaCert2048Pkcs    | 2048    | enabled       | PKCS12 | CN=example.com |
      | 7.3 | without | 73-createRsaCert4096Pkcs    | 4096    | enabled       | PKCS12 | CN=example.com |
      | 7.3 | with    | 73-createRsaCert2048PemHsm  | 2048    | enabled       | PEM    | CN=localhost   |
      | 7.3 | with    | 73-createRsaCert4096PemHsm  | 4096    | enabled       | PEM    | CN=localhost   |
      | 7.3 | with    | 73-createRsaCert2048PkcsHsm | 2048    | enabled       | PKCS12 | CN=example.com |
      | 7.3 | with    | 73-createRsaCert4096PkcsHsm | 4096    | enabled       | PKCS12 | CN=example.com |
