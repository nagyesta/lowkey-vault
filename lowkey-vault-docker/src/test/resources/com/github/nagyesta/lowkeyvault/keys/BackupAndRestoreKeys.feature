Feature: Key backup and restore

    @Key @KeyImport @KeySign @KeyBackup @KeyRestore @RSA
    Scenario Outline: RSA_BACKUP_01 An RSA key is imported, backed up, vault is recreated then after restore, the key is verified
        Given key API version <api> is used
        And a vault is created with name keys-backup-<keyName>
        And a key client is created with the vault named keys-backup-<keyName>
        And an RSA key is imported with <keyName> as name and <keySize> bits of key size without HSM
        And the key named <keyName> is backed up
        And the vault named keys-backup-<keyName> is deleted
        And the vault named keys-backup-<keyName> is purged
        And a vault is created with name keys-backup-<keyName>
        When the key named <keyName> is restored
        Then the created key is used to sign <clearText> with <algorithm>
        And the signed value is not <clearText>
        And the RSA signature of <clearText> is verified using the original public key with <algorithm>
        And the signature matches
        And the key named <keyName> matches the previous backup

        Examples:
            | api | keyName         | keySize | algorithm | clearText                                    |
            | 7.2 | backupRsaKey-01 | 2048    | PS256     | The quick brown fox jumps over the lazy dog. |
            | 7.3 | backupRsaKey-02 | 2048    | PS256     | The quick brown fox jumps over the lazy dog. |
            | 7.3 | backupRsaKey-03 | 2048    | PS384     | <?xml version="1.0"?><none/>                 |
            | 7.3 | backupRsaKey-04 | 2048    | PS512     | The quick brown fox jumps over the lazy dog. |
            | 7.3 | backupRsaKey-05 | 4096    | RS256     | The quick brown fox jumps over the lazy dog. |
            | 7.3 | backupRsaKey-06 | 4096    | RS384     | <?xml version="1.0"?><none/>                 |
            | 7.3 | backupRsaKey-07 | 4096    | RS512     | The quick brown fox jumps over the lazy dog. |
            | 7.4 | backupRsaKey-08 | 2048    | PS256     | The quick brown fox jumps over the lazy dog. |

    @Key @KeyImport @KeySign @KeyBackup @KeyRestore @EC
    Scenario Outline: EC_BACKUP_01 An EC key is imported, backed up, vault is recreated then after restore, the key is verified
        Given key API version <api> is used
        And a vault is created with name keys-backup-<keyName>
        And a key client is created with the vault named keys-backup-<keyName>
        And an EC key is imported with <keyName> as name and <curveName> curve without HSM
        And the key named <keyName> is backed up
        And the vault named keys-backup-<keyName> is deleted
        And the vault named keys-backup-<keyName> is purged
        And a vault is created with name keys-backup-<keyName>
        When the key named <keyName> is restored
        Then the created key is used to sign <clearText> with <algorithm>
        And the signed value is not <clearText>
        And the EC signature of <clearText> is verified using the original public key with <algorithm>
        And the signature matches
        And the key named <keyName> matches the previous backup

        Examples:
            | api | keyName     | curveName | algorithm | clearText                                                        |
            | 7.2 | backupEc-01 | P-256     | ES256     | The quick brown fox jumps over the lazy dog.                     |
            | 7.3 | backupEc-02 | P-256     | ES256     | The quick brown fox jumps over the lazy dog.                     |
            | 7.3 | backupEc-03 | P-256K    | ES256K    | The quick brown fox jumps over the lazy dog.                     |
            | 7.3 | backupEc-04 | P-384     | ES384     | The quick brown fox jumps over the lazy dog.                     |
            | 7.3 | backupEc-05 | P-521     | ES512     | The quick brown fox jumps over the lazy dog.                     |
            | 7.3 | backupEc-06 | P-256     | ES256     | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |
            | 7.3 | backupEc-07 | P-256K    | ES256K    | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |
            | 7.3 | backupEc-08 | P-384     | ES384     | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |
            | 7.3 | backupEc-09 | P-521     | ES512     | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |
            | 7.4 | backupEc-10 | P-256K    | ES256K    | The quick brown fox jumps over the lazy dog.                     |


    @Key @KeyImport @KeyEncrypt @KeyBackup @KeyRestore @OCT
    Scenario Outline: OCT_BACKUP_01 An OCT key is imported, backed up, vault is recreated then after restore, the key is verified
        Given key API version <api> is used
        And a vault is created with name keys-backup-<keyName>
        And a key client is created with the vault named keys-backup-<keyName>
        And an OCT key is imported with <keyName> as name and <keySize> bits of key size with HSM
        And the key named <keyName> is backed up
        And the vault named keys-backup-<keyName> is deleted
        And the vault named keys-backup-<keyName> is purged
        And a vault is created with name keys-backup-<keyName>
        When the key named <keyName> is restored
        Then the created key is used to encrypt <clearText> with <algorithm>
        And the encrypted value is not <clearText>
        And the encrypted value is decrypted using the original OCT key using <algorithm>
        And the decrypted value is <clearText>
        And the key named <keyName> matches the previous backup

        Examples:
            | api | keyName      | keySize | algorithm  | clearText                                                        |
            | 7.2 | backupOct-01 | 128     | A128CBCPAD | The quick brown fox jumps over the lazy dog.                     |
            | 7.3 | backupOct-02 | 128     | A128CBCPAD | The quick brown fox jumps over the lazy dog.                     |
            | 7.3 | backupOct-03 | 192     | A192CBCPAD | The quick brown fox jumps over the lazy dog.                     |
            | 7.3 | backupOct-04 | 256     | A256CBCPAD | The quick brown fox jumps over the lazy dog.                     |
            | 7.3 | backupOct-05 | 128     | A128CBC    | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |
            | 7.3 | backupOct-06 | 192     | A192CBC    | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |
            | 7.3 | backupOct-07 | 256     | A256CBC    | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |
            | 7.4 | backupOct-08 | 256     | A256CBC    | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |

    @Key @KeyImport @KeyEncrypt @KeyBackup @KeyRestore @RSA
    Scenario Outline: RSA_BACKUP_02 An RSA key is restored from json, backed up, then the backup content is compared to the source
        Given key API version <api> is used
        And a vault is created with name keys-backup-<keyName>
        And a key client is created with the vault named keys-backup-<keyName>
        And the key named <keyName> is restored from classpath resource
        When the key named <keyName> is backed up
        And the unpacked backup of <keyName> matches the content of the classpath resource

        Examples:
            | api | keyName               |
            | 7.2 | jsonBackupRsa-2048-72 |
            | 7.2 | jsonBackupRsa-3072-72 |
            | 7.2 | jsonBackupRsa-4096-72 |
            | 7.3 | jsonBackupRsa-2048-73 |
            | 7.3 | jsonBackupRsa-3072-73 |
            | 7.3 | jsonBackupRsa-4096-73 |
            | 7.4 | jsonBackupRsa-2048-74 |

    @Key @KeyImport @KeyEncrypt @KeyBackup @KeyRestore @EC
    Scenario Outline: EC_BACKUP_02 An EC key is restored from json, backed up, then the backup content is compared to the source
        Given key API version <api> is used
        And a vault is created with name keys-backup-<keyName>
        And a key client is created with the vault named keys-backup-<keyName>
        And the key named <keyName> is restored from classpath resource
        When the key named <keyName> is backed up
        And the unpacked backup of <keyName> matches the content of the classpath resource

        Examples:
            | api | keyName              |
            | 7.2 | jsonBackupEc-256-72  |
            | 7.2 | jsonBackupEc-256k-72 |
            | 7.2 | jsonBackupEc-384-72  |
            | 7.2 | jsonBackupEc-521-72  |
            | 7.3 | jsonBackupEc-256-73  |
            | 7.3 | jsonBackupEc-256k-73 |
            | 7.3 | jsonBackupEc-384-73  |
            | 7.3 | jsonBackupEc-521-73  |
            | 7.4 | jsonBackupEc-384-74  |

    @Key @KeyImport @KeyEncrypt @KeyBackup @KeyRestore @OCT
    Scenario Outline: OCT_BACKUP_02 An OCT key is restored from json, backed up, then the backup content is compared to the source
        Given key API version <api> is used
        And a vault is created with name keys-backup-<keyName>
        And a key client is created with the vault named keys-backup-<keyName>
        And the key named <keyName> is restored from classpath resource
        When the key named <keyName> is backed up
        And the unpacked backup of <keyName> matches the content of the classpath resource

        Examples:
            | api | keyName              |
            | 7.2 | jsonBackupOct-128-72 |
            | 7.2 | jsonBackupOct-192-72 |
            | 7.2 | jsonBackupOct-256-72 |
            | 7.3 | jsonBackupOct-128-73 |
            | 7.3 | jsonBackupOct-192-73 |
            | 7.3 | jsonBackupOct-256-73 |
            | 7.4 | jsonBackupOct-192-74 |
