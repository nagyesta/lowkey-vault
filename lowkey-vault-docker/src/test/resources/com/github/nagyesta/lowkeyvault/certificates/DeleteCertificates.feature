Feature: Certificate delete/purge/recover

    @Certificate @CertificateImport @CertificateDelete @RSA @CreateVault
    Scenario Outline: RSA_CERT_DELETE_01 Single versions of multiple RSA certificates imported and deleted then get as deleted
        Given certificate API version <api> is used
        And a vault is created with name cert-del-rsa-<index>
        And a certificate client is created with the vault named cert-del-rsa-<index>
        And <index> certificates are imported from the resource named <fileName> using - as password
        And the certificate policy named multi-import-<index> is downloaded
        When <index> certificates with multi-import- prefix are deleted
        Then the deleted certificate policy named multi-import-<index> is downloaded

        Examples:
            | api | index | fileName          |
            | 7.3 | 1     | rsa-localhost.pem |
            | 7.4 | 2     | rsa-localhost.pem |
            | 7.5 | 3     | rsa-localhost.pem |
            | 7.6 | 4     | rsa-localhost.pem |

    @Certificate @CertificateImport @CertificateDelete @EC @CreateVault
    Scenario Outline: EC_CERT_DELETE_01 Single versions of multiple EC certificates imported and deleted then get as deleted
        Given certificate API version <api> is used
        And a vault is created with name cert-del-ec-<index>
        And a certificate client is created with the vault named cert-del-ec-<index>
        And <index> certificates are imported from the resource named <fileName> using - as password
        And the certificate policy named multi-import-<index> is downloaded
        When <index> certificates with multi-import- prefix are deleted
        Then the deleted certificate policy named multi-import-<index> is downloaded

        Examples:
            | api | index | fileName               |
            | 7.3 | 1     | ec521-ec-localhost.pem |
            | 7.4 | 2     | ec521-ec-localhost.pem |
            | 7.5 | 3     | ec521-ec-localhost.pem |
            | 7.6 | 4     | ec521-ec-localhost.pem |

    @Certificate @CertificateImport @CertificateDelete @RSA @CreateVault
    Scenario Outline: RSA_CERT_PURGE_01 Single versions of multiple RSA certificates imported and deleted then purged
        Given certificate API version <api> is used
        And a vault is created with name cert-purge-rsa-<index>
        And a certificate client is created with the vault named cert-purge-rsa-<index>
        And <index> certificates are imported from the resource named <fileName> using - as password
        When <index> certificates with multi-import- prefix are deleted
        And <index> certificates with multi-import- prefix are purged
        Then the deleted certificates are listed
        And the deleted list should contain 0 items
        And the certificates are listed
        And the list of certificates should contain 0 items

        Examples:
            | api | index | fileName          |
            | 7.3 | 1     | rsa-localhost.pem |
            | 7.4 | 2     | rsa-localhost.pem |
            | 7.5 | 3     | rsa-localhost.pem |
            | 7.6 | 4     | rsa-localhost.pem |

    @Certificate @CertificateImport @CertificateDelete @EC @CreateVault
    Scenario Outline: EC_CERT_PURGE_01 Single versions of multiple EC certificates imported and deleted then purged
        Given certificate API version <api> is used
        And a vault is created with name cert-purge-ec-<index>
        And a certificate client is created with the vault named cert-purge-ec-<index>
        And <index> certificates are imported from the resource named <fileName> using - as password
        When <index> certificates with multi-import- prefix are deleted
        And <index> certificates with multi-import- prefix are purged
        Then the deleted certificates are listed
        And the deleted list should contain 0 items
        And the certificates are listed
        And the list of certificates should contain 0 items

        Examples:
            | api | index | fileName               |
            | 7.3 | 1     | ec521-ec-localhost.pem |
            | 7.4 | 2     | ec521-ec-localhost.pem |
            | 7.5 | 3     | ec521-ec-localhost.pem |
            | 7.6 | 4     | ec521-ec-localhost.pem |

    @Certificate @CertificateImport @CertificateDelete @RSA @CreateVault
    Scenario Outline: RSA_CERT_RECOVER_01 Single versions of multiple RSA certificates imported and deleted then recovered
        Given certificate API version <api> is used
        And a vault is created with name cert-recover-rsa-<index>
        And a certificate client is created with the vault named cert-recover-rsa-<index>
        And <index> certificates are imported from the resource named <fileName> using - as password
        When <index> certificates with multi-import- prefix are deleted
        And <index> certificates with multi-import- prefix are recovered
        Then the deleted certificates are listed
        And the deleted list should contain 0 items
        And the certificates are listed
        And the list of certificates should contain <index> items

        Examples:
            | api | index | fileName          |
            | 7.3 | 1     | rsa-localhost.pem |
            | 7.4 | 2     | rsa-localhost.pem |
            | 7.5 | 3     | rsa-localhost.pem |
            | 7.6 | 4     | rsa-localhost.pem |

    @Certificate @CertificateImport @CertificateDelete @EC @CreateVault
    Scenario Outline: EC_CERT_RECOVER_01 Single versions of multiple EC certificates imported and deleted then recovered
        Given certificate API version <api> is used
        And a vault is created with name cert-recover-ec-<index>
        And a certificate client is created with the vault named cert-recover-ec-<index>
        And <index> certificates are imported from the resource named <fileName> using - as password
        When <index> certificates with multi-import- prefix are deleted
        And <index> certificates with multi-import- prefix are recovered
        Then the deleted certificates are listed
        And the deleted list should contain 0 items
        And the certificates are listed
        And the list of certificates should contain <index> items

        Examples:
            | api | index | fileName               |
            | 7.3 | 1     | ec521-ec-localhost.pem |
            | 7.4 | 2     | ec521-ec-localhost.pem |
            | 7.5 | 3     | ec521-ec-localhost.pem |
            | 7.6 | 4     | ec521-ec-localhost.pem |
