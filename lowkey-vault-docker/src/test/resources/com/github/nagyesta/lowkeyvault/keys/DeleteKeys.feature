Feature: Key delete and recover

  @Key @KeyCreate @KeyDelete @RSA @KeyAlias
  Scenario Outline: RSA_DELETE_01 Multiple versions of RSA keys are created with the key client then deleted
    Given key API version <api> is used
    And a key client is created with the vault named <vaultName>
    And an RSA key named <keyName> is prepared with <keySize> bits size <hsm> HSM
    And <versionsCount> version of the RSA key is created
    When the key is deleted
    Then the deleted key recovery id contains the vault url and <keyName>
    And the key recovery timestamps are default

    Examples:
      | api | vaultName         | versionsCount | hsm     | keyName                 | keySize |
      | 7.2 | keys-delete       | 5             | without | 72-deleteRsaKey         | 2048    |
      | 7.2 | keys-delete       | 6             | without | 72-delete-rsa-key-name  | 2048    |
      | 7.3 | keys-delete       | 5             | without | 73-deleteRsaKey         | 2048    |
      | 7.3 | keys-delete       | 6             | without | 73-delete-rsa-key-name  | 2048    |
      | 7.3 | keys-alias-delete | 5             | without | 73-deleteRsaKeyA        | 2048    |
      | 7.3 | keys-alias-delete | 6             | without | 73-delete-rsa-key-nameA | 2048    |

  @Key @KeyCreate @KeyDelete @EC @KeyAlias
  Scenario Outline: EC_DELETE_01 Multiple versions of EC keys are created with the key client then deleted
    Given key API version <api> is used
    And a key client is created with the vault named <vaultName>
    And an EC key named <keyName> is prepared with <curveName> and <hsm> HSM
    And <versionsCount> version of the EC key is created
    When the key is deleted
    Then the deleted key recovery id contains the vault url and <keyName>
    And the key recovery timestamps are default

    Examples:
      | api | vaultName         | versionsCount | hsm     | keyName             | curveName |
      | 7.2 | keys-delete       | 5             | without | 72-deleteEcKey256   | P-256     |
      | 7.2 | keys-delete       | 6             | without | 72-deleteEcKey256k  | P-256K    |
      | 7.3 | keys-delete       | 5             | without | 73-deleteEcKey256   | P-256     |
      | 7.3 | keys-delete       | 6             | without | 73-deleteEcKey256k  | P-256K    |
      | 7.3 | keys-alias-delete | 5             | without | 73-deleteEcKey256A  | P-256     |
      | 7.3 | keys-alias-delete | 6             | without | 73-deleteEcKey256kA | P-256K    |

  @Key @KeyCreate @KeyDelete @OCT @KeyAlias
  Scenario Outline: OCT_DELETE_01 Multiple versions of OCT keys are created with the key client then deleted
    Given key API version <api> is used
    And a key client is created with the vault named <vaultName>
    And an OCT key named <keyName> is prepared with <keySize> bits size
    And <versionsCount> version of the OCT key is created
    When the key is deleted
    Then the deleted key recovery id contains the vault url and <keyName>
    And the key recovery timestamps are default

    Examples:
      | api | vaultName         | versionsCount | keyName             | keySize |
      | 7.2 | keys-delete       | 5             | 72-deleteOctKey     | 128     |
      | 7.2 | keys-delete       | 6             | 72-deleteOctKey192  | 192     |
      | 7.3 | keys-delete       | 5             | 73-deleteOctKey     | 128     |
      | 7.3 | keys-delete       | 6             | 73-deleteOctKey192  | 192     |
      | 7.3 | keys-alias-delete | 5             | 73-deleteOctKeyA    | 128     |
      | 7.3 | keys-alias-delete | 6             | 73-deleteOctKey192A | 192     |

  @Key @KeyCreate @KeyDelete @KeyRecover @RSA @KeyAlias
  Scenario Outline: RSA_RECOVER_01 Multiple versions of RSA keys are created with the key client then deleted and recovered
    Given key API version <api> is used
    And a key client is created with the vault named <vaultName>
    And an RSA key named <keyName> is prepared with <keySize> bits size <hsm> HSM
    And <versionsCount> version of the RSA key is created
    And the key is deleted
    When the key is recovered
    Then the key URL contains the vault url and <keyName>

    Examples:
      | api | vaultName         | versionsCount | hsm     | keyName                  | keySize |
      | 7.2 | keys-delete       | 5             | without | 72-recoverRsaKey         | 2048    |
      | 7.2 | keys-delete       | 6             | without | 72-recover-rsa-key-name  | 2048    |
      | 7.3 | keys-delete       | 5             | without | 73-recoverRsaKey         | 2048    |
      | 7.3 | keys-delete       | 6             | without | 73-recover-rsa-key-name  | 2048    |
      | 7.3 | keys-alias-delete | 5             | without | 73-recoverRsaKeyA        | 2048    |
      | 7.3 | keys-alias-delete | 6             | without | 73-recover-rsa-key-nameA | 2048    |

  @Key @KeyCreate @KeyDelete @KeyRecover @EC @KeyAlias
  Scenario Outline: EC_RECOVER_01 Multiple versions of EC keys are created with the key client then deleted and recovered
    Given key API version <api> is used
    And a key client is created with the vault named <vaultName>
    And an EC key named <keyName> is prepared with <curveName> and <hsm> HSM
    And <versionsCount> version of the EC key is created
    And the key is deleted
    When the key is recovered
    Then the key URL contains the vault url and <keyName>

    Examples:
      | api | vaultName         | versionsCount | hsm     | keyName              | curveName |
      | 7.2 | keys-delete       | 5             | without | 72-recoverEcKey256   | P-256     |
      | 7.2 | keys-delete       | 6             | without | 72-recoverEcKey256k  | P-256K    |
      | 7.3 | keys-delete       | 5             | without | 73-recoverEcKey256   | P-256     |
      | 7.3 | keys-delete       | 6             | without | 73-recoverEcKey256k  | P-256K    |
      | 7.3 | keys-alias-delete | 5             | without | 73-recoverEcKey256A  | P-256     |
      | 7.3 | keys-alias-delete | 6             | without | 73-recoverEcKey256kA | P-256K    |

  @Key @KeyCreate @KeyDelete @KeyRecover @OCT @KeyAlias
  Scenario Outline: OCT_RECOVER_01 Multiple versions of OCT keys are created with the key client then deleted and recovered
    Given key API version <api> is used
    And a key client is created with the vault named <vaultName>
    And an OCT key named <keyName> is prepared with <keySize> bits size
    And <versionsCount> version of the OCT key is created
    And the key is deleted
    When the key is recovered
    Then the key URL contains the vault url and <keyName>

    Examples:
      | api | vaultName   | versionsCount | keyName             | keySize |
      | 7.2 | keys-delete | 5             | 72-recoverOctKey    | 128     |
      | 7.2 | keys-delete | 6             | 72-recoverOctKey192 | 192     |
      | 7.3 | keys-delete | 5             | 73-recoverOctKey    | 128     |
      | 7.3 | keys-delete | 6             | 73-recoverOctKey192 | 192     |

  @Key @KeyCreate @KeyDelete @KeyPurge @RSA
  Scenario Outline: RSA_PURGE_01 Multiple versions of RSA keys are created with the key client then deleted and purged
    Given key API version <api> is used
    And a vault is created with name keys-purge-<keyName>
    And a key client is created with the vault named keys-purge-<keyName>
    And an RSA key named <keyName> is prepared with <keySize> bits size <hsm> HSM
    And <versionsCount> version of the RSA key is created
    And the key is deleted
    When the key is purged
    Then the deleted key properties are listed
    And the listed deleted keys are empty
    And the vault named keys-purge-<keyName> is deleted
    And the vault named keys-purge-<keyName> is purged

    Examples:
      | api | versionsCount | hsm     | keyName               | keySize |
      | 7.2 | 5             | without | 72-purgeRsaKey        | 2048    |
      | 7.2 | 6             | without | 72-purge-rsa-key-name | 2048    |
      | 7.3 | 5             | without | 73-purgeRsaKey        | 2048    |
      | 7.3 | 6             | without | 73-purge-rsa-key-name | 2048    |

  @Key @KeyCreate @KeyDelete @KeyPurge @EC
  Scenario Outline: EC_PURGE_01 Multiple versions of EC keys are created with the key client then deleted and purge
    Given key API version <api> is used
    And a vault is created with name keys-purge-<keyName>
    And a key client is created with the vault named keys-purge-<keyName>
    And an EC key named <keyName> is prepared with <curveName> and <hsm> HSM
    And <versionsCount> version of the EC key is created
    And the key is deleted
    When the key is purged
    Then the deleted key properties are listed
    And the listed deleted keys are empty
    And the vault named keys-purge-<keyName> is deleted
    And the vault named keys-purge-<keyName> is purged

    Examples:
      | api | versionsCount | hsm     | keyName           | curveName |
      | 7.2 | 5             | without | 72-purgeEcKey256  | P-256     |
      | 7.2 | 6             | without | 72-purgeEcKey256k | P-256K    |
      | 7.3 | 5             | without | 73-purgeEcKey256  | P-256     |
      | 7.3 | 6             | without | 73-purgeEcKey256k | P-256K    |

  @Key @KeyCreate @KeyDelete @KeyPurge @OCT
  Scenario Outline: OCT_PURGE_01 Multiple versions of OCT keys are created with the key client then deleted and purge
    Given key API version <api> is used
    And a vault is created with name keys-purge-<keyName>
    And a key client is created with the vault named keys-purge-<keyName>
    And an OCT key named <keyName> is prepared with <keySize> bits size
    And <versionsCount> version of the OCT key is created
    And the key is deleted
    When the key is purged
    Then the deleted key properties are listed
    And the listed deleted keys are empty
    And the vault named keys-purge-<keyName> is deleted
    And the vault named keys-purge-<keyName> is purged

    Examples:
      | api | versionsCount | keyName           | keySize |
      | 7.2 | 3             | 72-purgeOctKey    | 128     |
      | 7.2 | 4             | 72-purgeOctKey192 | 192     |
      | 7.3 | 3             | 73-purgeOctKey    | 128     |
      | 7.3 | 4             | 73-purgeOctKey192 | 192     |
