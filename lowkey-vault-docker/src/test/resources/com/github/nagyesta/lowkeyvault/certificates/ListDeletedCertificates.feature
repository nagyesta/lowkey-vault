Feature: Certificate list deleted

    @Certificate @CertificateImport @CertificateListDeleted @RSA @CreateVault
    Scenario Outline: RSA_CERT_LIST_DELETED_01 Single versions of multiple RSA certificates imported and deleted then listed as deleted
        Given certificate API version <api> is used
        And a vault is created with name cert-list-deleted-rsa-<index>
        And a certificate client is created with the vault named cert-list-deleted-rsa-<index>
        And <count> certificates are imported from the resource named <fileName> using - as password
        And <count> certificates with multi-import- prefix are deleted
        When the deleted certificates are listed
        Then the deleted list should contain <count> items

        Examples:
            | api | index | fileName          | count |
            | 7.3 | 1     | rsa-localhost.pem | 1     |
            | 7.3 | 2     | rsa-localhost.pem | 5     |
            | 7.4 | 3     | rsa-localhost.pem | 5     |

    @Certificate @CertificateImport @CertificateListDeleted @EC @CreateVault
    Scenario Outline: EC_CERT_LIST_DELETED_01 Single versions of multiple EC certificates imported and deleted then listed as deleted
        Given certificate API version <api> is used
        And a vault is created with name cert-list-deleted-ec-<index>
        And a certificate client is created with the vault named cert-list-deleted-ec-<index>
        And <count> certificates are imported from the resource named <fileName> using - as password
        And <count> certificates with multi-import- prefix are deleted
        When the deleted certificates are listed
        Then the deleted list should contain <count> items

        Examples:
            | api | index | fileName               | count |
            | 7.3 | 1     | ec521-ec-localhost.pem | 1     |
            | 7.3 | 2     | ec521-ec-localhost.pem | 5     |
            | 7.4 | 3     | ec521-ec-localhost.pem | 1     |
