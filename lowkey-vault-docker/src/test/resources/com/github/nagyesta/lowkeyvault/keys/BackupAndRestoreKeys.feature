Feature: Key backup and restore

  @Key @KeyImport @KeySign @KeyBackup @KeyRestore @RSA
  Scenario Outline: RSA_BACKUP_01 An RSA key is imported, backed up, vault is recreated then after restore, the key is verified
    Given a vault is created with name keys-backup-<keyName>
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
      | keyName        | keySize | algorithm | clearText                                    |
      | backupRsaKey-1 | 2048    | PS256     | The quick brown fox jumps over the lazy dog. |
      | backupRsaKey-2 | 2048    | PS384     | <?xml version="1.0"?><none/>                 |
      | backupRsaKey-3 | 2048    | PS512     | The quick brown fox jumps over the lazy dog. |
      | backupRsaKey-4 | 4096    | RS256     | The quick brown fox jumps over the lazy dog. |
      | backupRsaKey-5 | 4096    | RS384     | <?xml version="1.0"?><none/>                 |
      | backupRsaKey-6 | 4096    | RS512     | The quick brown fox jumps over the lazy dog. |

  @Key @KeyImport @KeySign @KeyBackup @KeyRestore @EC
  Scenario Outline: EC_BACKUP_01 An EC key is imported, backed up, vault is recreated then after restore, the key is verified
    Given a vault is created with name keys-backup-<keyName>
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
      | keyName    | curveName | algorithm | clearText                                                        |
      | backupEc-1 | P-256     | ES256     | The quick brown fox jumps over the lazy dog.                     |
      | backupEc-2 | P-256K    | ES256K    | The quick brown fox jumps over the lazy dog.                     |
      | backupEc-3 | P-384     | ES384     | The quick brown fox jumps over the lazy dog.                     |
      | backupEc-4 | P-521     | ES512     | The quick brown fox jumps over the lazy dog.                     |
      | backupEc-5 | P-256     | ES256     | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |
      | backupEc-6 | P-256K    | ES256K    | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |
      | backupEc-7 | P-384     | ES384     | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |
      | backupEc-8 | P-521     | ES512     | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |


  @Key @KeyImport @KeyEncrypt @KeyBackup @KeyRestore @OCT
  Scenario Outline: OCT_BACKUP_01 An OCT key is imported, backed up, vault is recreated then after restore, the key is verified
    Given a vault is created with name keys-backup-<keyName>
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
      | keyName     | keySize | algorithm  | clearText                                                        |
      | backupOct-1 | 128     | A128CBCPAD | The quick brown fox jumps over the lazy dog.                     |
      | backupOct-2 | 192     | A192CBCPAD | The quick brown fox jumps over the lazy dog.                     |
      | backupOct-3 | 256     | A256CBCPAD | The quick brown fox jumps over the lazy dog.                     |
      | backupOct-4 | 128     | A128CBC    | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |
      | backupOct-5 | 192     | A192CBC    | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |
      | backupOct-6 | 256     | A256CBC    | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |
