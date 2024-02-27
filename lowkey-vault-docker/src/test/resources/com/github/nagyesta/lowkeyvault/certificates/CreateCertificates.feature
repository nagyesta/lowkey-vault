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
        And the last certificate version of <certName> is fetched without providing a version
        And the lifetime action triggers AutoRenew when 80 percent lifetime reached

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
            | 7.4 | without | 74-createRsaCert2048Pem     | 2048    | enabled       | PEM    | CN=localhost   |
            | 7.4 | with    | 74-createRsaCert2048PemHsm  | 2048    | enabled       | PEM    | CN=localhost   |
            | 7.5 | without | 75-createRsaCert2048Pem     | 2048    | enabled       | PEM    | CN=localhost   |
            | 7.5 | with    | 75-createRsaCert2048PemHsm  | 2048    | enabled       | PEM    | CN=localhost   |

    @Certificate @CertificateCreate @EC
    Scenario Outline: EC_CERT_CREATE_01 Single versions of EC certificates can be created with the certificate client
        Given certificate API version <api> is used
        And a certificate client is created with the vault named certs-generic
        And a <type> certificate is prepared with subject <subject>
        And the certificate is set to be <enabledStatus>
        And the certificate is set to use an EC key with <curveName> and <hsm> HSM
        When the certificate is created with name <certName>
        Then the certificate is <enabledStatus>
        And the certificate secret named <certName> is downloaded
        And the downloaded secret contains a <type> certificate
        And the downloaded <type> certificate store has a certificate with <subject> as subject
        And the lifetime action triggers AutoRenew when 80 percent lifetime reached

        Examples:
            | api | hsm     | certName                    | curveName | enabledStatus | type   | subject        |
            | 7.3 | without | 73-createEcCertP256Pem      | P-256     | enabled       | PEM    | CN=localhost   |
            | 7.3 | without | 73-createEcCertP256KPem     | P-256K    | enabled       | PEM    | CN=localhost   |
            | 7.3 | without | 73-createEcCertP384Pem      | P-384     | enabled       | PEM    | CN=localhost   |
            | 7.3 | without | 73-createEcCertP521Pem      | P-521     | enabled       | PEM    | CN=localhost   |
            | 7.3 | without | 73-createEcCertP256Pkcs     | P-256     | enabled       | PKCS12 | CN=example.com |
            | 7.3 | without | 73-createEcCertP256KPkcs    | P-256K    | enabled       | PKCS12 | CN=example.com |
            | 7.3 | without | 73-createEcCertP384Pkcs     | P-384     | enabled       | PKCS12 | CN=example.com |
            | 7.3 | without | 73-createEcCertP521Pkcs     | P-521     | enabled       | PKCS12 | CN=example.com |
            | 7.3 | with    | 73-createEcCertP256PemHsm   | P-256     | enabled       | PEM    | CN=localhost   |
            | 7.3 | with    | 73-createEcCertP256KPemHsm  | P-256K    | enabled       | PEM    | CN=localhost   |
            | 7.3 | with    | 73-createEcCertP384PemHsm   | P-384     | enabled       | PEM    | CN=localhost   |
            | 7.3 | with    | 73-createEcCertP521PemHsm   | P-521     | enabled       | PEM    | CN=localhost   |
            | 7.3 | with    | 73-createEcCertP256PkcsHsm  | P-256     | enabled       | PKCS12 | CN=example.com |
            | 7.3 | with    | 73-createEcCertP256KPkcsHsm | P-256K    | enabled       | PKCS12 | CN=example.com |
            | 7.3 | with    | 73-createEcCertP384PkcsHsm  | P-384     | enabled       | PKCS12 | CN=example.com |
            | 7.3 | with    | 73-createEcCertP521PkcsHsm  | P-521     | enabled       | PKCS12 | CN=example.com |
            | 7.4 | without | 74-createEcCertP256Pem      | P-256     | enabled       | PEM    | CN=localhost   |
            | 7.4 | with    | 74-createEcCertP256PemHsm   | P-256     | enabled       | PEM    | CN=localhost   |
            | 7.5 | without | 75-createEcCertP256Pem      | P-256     | enabled       | PEM    | CN=localhost   |
            | 7.5 | with    | 75-createEcCertP256PemHsm   | P-256     | enabled       | PEM    | CN=localhost   |

    @Certificate @CertificateCreate @RSA
    Scenario Outline: RSA_CERT_CREATE_02 Single versions of RSA certificates can be created using lifetime actions
        Given certificate API version <api> is used
        And a certificate client is created with the vault named certs-generic
        And a <type> certificate is prepared with subject CN=localhost
        And the lifetime action trigger is set to <action> when <triggerValue> <triggerType> reached
        And the certificate is set to be enabled
        And the certificate is set to use an RSA key with 2048 and without HSM
        When the certificate is created with name <certName>
        Then the certificate is enabled
        And the lifetime action triggers <action> when <triggerValue> <triggerType> reached
        And the certificate secret named <certName> is downloaded
        And the downloaded secret contains a <type> certificate
        And the downloaded <type> certificate store has a certificate with CN=localhost as subject

        Examples:
            | api | certName                       | triggerValue | triggerType        | action        | type   |
            | 7.3 | 73-createRsaCertPemAction      | 20           | days before expiry | EmailContacts | PEM    |
            | 7.3 | 73-createRsaCertPkcsAction     | 75           | percent lifetime   | AutoRenew     | PKCS12 |
            | 7.3 | 73-createRsaCertPemRenewAction | 75           | percent lifetime   | AutoRenew     | PEM    |
            | 7.4 | 74-createRsaCertPemAction      | 20           | days before expiry | EmailContacts | PEM    |
            | 7.5 | 75-createRsaCertPemAction      | 20           | days before expiry | EmailContacts | PEM    |

    @Certificate @CertificateCreate @EC
    Scenario Outline: EC_CERT_CREATE_02 Single versions of EC certificates can be created using lifetime actions
        Given certificate API version <api> is used
        And a certificate client is created with the vault named certs-generic
        And a <type> certificate is prepared with subject CN=localhost
        And the lifetime action trigger is set to <action> when <triggerValue> <triggerType> reached
        And the certificate is set to be enabled
        And the certificate is set to use an EC key with P-256 and without HSM
        When the certificate is created with name <certName>
        Then the certificate is enabled
        And the lifetime action triggers <action> when <triggerValue> <triggerType> reached
        And the certificate secret named <certName> is downloaded
        And the downloaded secret contains a <type> certificate
        And the downloaded <type> certificate store has a certificate with CN=localhost as subject

        Examples:
            | api | certName                  | triggerValue | triggerType        | action        | type   |
            | 7.3 | 73-createEcCertPemAction  | 10           | days before expiry | EmailContacts | PEM    |
            | 7.3 | 73-createEcCertPkcsAction | 80           | percent lifetime   | AutoRenew     | PKCS12 |
            | 7.4 | 74-createEcCertPemAction  | 10           | days before expiry | EmailContacts | PEM    |
            | 7.5 | 75-createEcCertPemAction  | 10           | days before expiry | EmailContacts | PEM    |

    @Certificate @CertificateCreate @RSA
    Scenario Outline: RSA_CERT_CREATE_03 Two versions of the same RSA certificates can be created using the same name
        Given certificate API version <api> is used
        And a certificate client is created with the vault named certs-generic
        And a <type> certificate is prepared with subject CN=localhost
        And the certificate is set to be enabled
        And the certificate is set to use an RSA key with 2048 and without HSM
        And the certificate is created with name <certName>
            # create a second version
        When the certificate is created with name <certName>
        Then the certificate is enabled
        And the certificate secret named <certName> is downloaded
        And the downloaded secret contains a <type> certificate
        And the downloaded <type> certificate store has a certificate with CN=localhost as subject

        Examples:
            | api | certName                   | type   |
            | 7.3 | 73-createRsaCertPemDouble  | PEM    |
            | 7.3 | 73-createRsaCertPkcsDouble | PKCS12 |
            | 7.4 | 74-createRsaCertPemDouble  | PEM    |
            | 7.5 | 75-createRsaCertPemDouble  | PEM    |

    @Certificate @CertificateCreate @EC
    Scenario Outline: EC_CERT_CREATE_03 Two versions of the same EC certificates can be created using the same name
        Given certificate API version <api> is used
        And a certificate client is created with the vault named certs-generic
        And a <type> certificate is prepared with subject CN=localhost
        And the certificate is set to be enabled
        And the certificate is set to use an EC key with P-256 and without HSM
        And the certificate is created with name <certName>
            # create a second version
        When the certificate is created with name <certName>
        Then the certificate is enabled
        And the certificate secret named <certName> is downloaded
        And the downloaded secret contains a <type> certificate
        And the downloaded <type> certificate store has a certificate with CN=localhost as subject

        Examples:
            | api | certName                  | type   |
            | 7.3 | 73-createEcCertPemDouble  | PEM    |
            | 7.3 | 73-createEcCertPkcsDouble | PKCS12 |
            | 7.4 | 74-createEcCertPemDouble  | PEM    |
            | 7.5 | 75-createEcCertPemDouble  | PEM    |
