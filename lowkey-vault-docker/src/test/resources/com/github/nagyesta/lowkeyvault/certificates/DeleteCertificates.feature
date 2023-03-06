Feature: Certificate delete/purge/recover

    @Certificate @CertificateImport @CertificateDelete @RSA @CreateVault
    Scenario Outline: RSA_CERT_DELETE_01 Single versions of multiple RSA certificates imported and deleted then get as deleted
        Given certificate API version <api> is used
        And a vault is created with name cert-del-rsa-<index>
        And a certificate client is created with the vault named cert-del-rsa-<index>
        And 1 certificates are imported from the resource named <fileName> using - as password
        When 1 certificates with multi-import- prefix are deleted
        Then the deleted certificate policy named multi-import-0 is downloaded

        Examples:
            | api | index | fileName          |
            | 7.3 | 1     | rsa-localhost.pem |

    @Certificate @CertificateImport @CertificateDelete @EC @CreateVault
    Scenario Outline: EC_CERT_DELETE_01 Single versions of multiple EC certificates imported and deleted then get as deleted
        Given certificate API version <api> is used
        And a vault is created with name cert-del-ec-<index>
        And a certificate client is created with the vault named cert-del-ec-<index>
        And 1 certificates are imported from the resource named <fileName> using - as password
        When 1 certificates with multi-import- prefix are deleted
        Then the deleted certificate policy named multi-import-0 is downloaded

        Examples:
            | api | index | fileName               |
            | 7.3 | 1     | ec521-ec-localhost.pem |

    @Certificate @CertificateImport @CertificateDelete @RSA @CreateVault
    Scenario Outline: RSA_CERT_PURGE_01 Single versions of multiple RSA certificates imported and deleted then purged
        Given certificate API version <api> is used
        And a vault is created with name cert-purge-rsa-<index>
        And a certificate client is created with the vault named cert-purge-rsa-<index>
        And 1 certificates are imported from the resource named <fileName> using - as password
        When 1 certificates with multi-import- prefix are deleted
        And 1 certificates with multi-import- prefix are purged
        Then the deleted certificates are listed
        And the deleted list should contain 0 items
        And the certificates are listed
        And the list should contain 0 items

        Examples:
            | api | index | fileName          |
            | 7.3 | 1     | rsa-localhost.pem |

    @Certificate @CertificateImport @CertificateDelete @EC @CreateVault
    Scenario Outline: EC_CERT_PURGE_01 Single versions of multiple EC certificates imported and deleted then purged
        Given certificate API version <api> is used
        And a vault is created with name cert-purge-ec-<index>
        And a certificate client is created with the vault named cert-purge-ec-<index>
        And 1 certificates are imported from the resource named <fileName> using - as password
        When 1 certificates with multi-import- prefix are deleted
        And 1 certificates with multi-import- prefix are purged
        Then the deleted certificates are listed
        And the deleted list should contain 0 items
        And the certificates are listed
        And the list should contain 0 items

        Examples:
            | api | index | fileName               |
            | 7.3 | 1     | ec521-ec-localhost.pem |

    @Certificate @CertificateImport @CertificateDelete @RSA @CreateVault
    Scenario Outline: RSA_CERT_RECOVER_01 Single versions of multiple RSA certificates imported and deleted then recovered
        Given certificate API version <api> is used
        And a vault is created with name cert-recover-rsa-<index>
        And a certificate client is created with the vault named cert-recover-rsa-<index>
        And 1 certificates are imported from the resource named <fileName> using - as password
        When 1 certificates with multi-import- prefix are deleted
        And 1 certificates with multi-import- prefix are recovered
        Then the deleted certificates are listed
        And the deleted list should contain 0 items
        And the certificates are listed
        And the list should contain 1 items

        Examples:
            | api | index | fileName          |
            | 7.3 | 1     | rsa-localhost.pem |

    @Certificate @CertificateImport @CertificateDelete @EC @CreateVault
    Scenario Outline: EC_CERT_RECOVER_01 Single versions of multiple EC certificates imported and deleted then recovered
        Given certificate API version <api> is used
        And a vault is created with name cert-recover-ec-<index>
        And a certificate client is created with the vault named cert-recover-ec-<index>
        And 1 certificates are imported from the resource named <fileName> using - as password
        When 1 certificates with multi-import- prefix are deleted
        And 1 certificates with multi-import- prefix are recovered
        Then the deleted certificates are listed
        And the deleted list should contain 0 items
        And the certificates are listed
        And the list should contain 1 items

        Examples:
            | api | index | fileName               |
            | 7.3 | 1     | ec521-ec-localhost.pem |
