Feature: Certificate list

    @Certificate @CertificateImport @CertificateList @RSA @CreateVault
    Scenario Outline: RSA_CERT_LIST_01 Single versions of multiple RSA certificates imported then listed with the certificate client
        Given certificate API version <api> is used
        And a vault is created with name cert-list-rsa-<index>
        And a certificate client is created with the vault named cert-list-rsa-<index>
        And <count> certificates are imported from the resource named <fileName> using - as password
        When the certificates are listed
        Then the list should contain <count> items

        Examples:
            | api | index | fileName          | count |
            | 7.3 | 1     | rsa-localhost.pem | 60    |
            | 7.3 | 2     | rsa-localhost.pem | 26    |
            | 7.3 | 3     | rsa-localhost.pem | 2     |
            | 7.3 | 4     | rsa-localhost.pem | 10    |

    @Certificate @CertificateImport @CertificateList @EC @CreateVault
    Scenario Outline: EC_CERT_LIST_01 Single versions of multiple EC certificates imported then listed with the certificate client
        Given certificate API version <api> is used
        And a vault is created with name cert-list-ec-<index>
        And a certificate client is created with the vault named cert-list-ec-<index>
        And <count> certificates are imported from the resource named <fileName> using - as password
        When the certificates are listed
        Then the list should contain <count> items

        Examples:
            | api | index | fileName               | count |
            | 7.3 | 1     | ec521-ec-localhost.pem | 60    |
            | 7.3 | 2     | ec521-ec-localhost.pem | 26    |
            | 7.3 | 3     | ec521-ec-localhost.pem | 2     |
            | 7.3 | 4     | ec521-ec-localhost.pem | 10    |

    @Certificate @CertificateImport @CertificateList @RSA @CreateVault
    Scenario Outline: RSA_CERT_LIST_02 A single version of an RSA certificate is imported then versions listed with the certificate client
        Given certificate API version <api> is used
        And a vault is created with name cert-list-rsa-ver-<index>
        And a certificate client is created with the vault named cert-list-rsa-ver-<index>
        And 1 certificates are imported from the resource named <fileName> using - as password
        When the certificate versions are listed
        Then the list should contain 1 items

        Examples:
            | api | index | fileName          |
            | 7.3 | 1     | rsa-localhost.pem |

    @Certificate @CertificateImport @CertificateList @EC @CreateVault
    Scenario Outline: EC_CERT_LIST_02 A single version of an EC certificate is imported then versions listed with the certificate client
        Given certificate API version <api> is used
        And a vault is created with name cert-list-ec-ver-<index>
        And a certificate client is created with the vault named cert-list-ec-ver-<index>
        And 1 certificates are imported from the resource named <fileName> using - as password
        When the certificate versions are listed
        Then the list should contain 1 items

        Examples:
            | api | index | fileName               |
            | 7.3 | 1     | ec521-ec-localhost.pem |
