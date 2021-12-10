Feature: Key delete and recover

  @Key @KeyCreate @KeyDelete @RSA
  Scenario Outline: RSA_DELETE_01 Multiple versions of RSA keys are created with the key client then deleted
    Given a key client is created with the vault named keys-delete
    And an RSA key named <keyName> is prepared with <keySize> bits size <hsm> HSM
    And <versionsCount> version of the RSA key is created
    When the key is deleted
    Then the deleted key recovery id contains the vault url and <keyName>
    And the key recovery timestamps are default

    Examples:
      | versionsCount | hsm     | keyName             | keySize |
      | 5             | without | deleteRsaKey        | 2048    |
      | 6             | without | delete-rsa-key-name | 2048    |

  @Key @KeyCreate @KeyDelete @EC
  Scenario Outline: EC_DELETE_01 Multiple versions of EC keys are created with the key client then deleted
    Given a key client is created with the vault named keys-delete
    And an EC key named <keyName> is prepared with <curveName> and <hsm> HSM
    And <versionsCount> version of the EC key is created
    When the key is deleted
    Then the deleted key recovery id contains the vault url and <keyName>
    And the key recovery timestamps are default

    Examples:
      | versionsCount | hsm     | keyName         | curveName |
      | 5             | without | deleteEcKey256  | P-256     |
      | 6             | without | deleteEcKey256k | P-256K    |

  @Key @KeyCreate @KeyDelete @OCT
  Scenario Outline: OCT_DELETE_01 Multiple versions of OCT keys are created with the key client then deleted
    Given a key client is created with the vault named keys-delete
    And an OCT key named <keyName> is prepared with <keySize> bits size
    And <versionsCount> version of the OCT key is created
    When the key is deleted
    Then the deleted key recovery id contains the vault url and <keyName>
    And the key recovery timestamps are default

    Examples:
      | versionsCount | keyName         | keySize |
      | 5             | deleteOctKey    | 128     |
      | 6             | deleteOctKey192 | 192     |

  @Key @KeyCreate @KeyDelete @KeyRecover @RSA
  Scenario Outline: RSA_RECOVER_01 Multiple versions of RSA keys are created with the key client then deleted and recovered
    Given a key client is created with the vault named keys-delete
    And an RSA key named <keyName> is prepared with <keySize> bits size <hsm> HSM
    And <versionsCount> version of the RSA key is created
    And the key is deleted
    When the key is recovered
    Then the key URL contains the vault url and <keyName>

    Examples:
      | versionsCount | hsm     | keyName              | keySize |
      | 5             | without | recoverRsaKey        | 2048    |
      | 6             | without | recover-rsa-key-name | 2048    |

  @Key @KeyCreate @KeyDelete @KeyRecover @EC
  Scenario Outline: EC_RECOVER_01 Multiple versions of EC keys are created with the key client then deleted and recovered
    Given a key client is created with the vault named keys-delete
    And an EC key named <keyName> is prepared with <curveName> and <hsm> HSM
    And <versionsCount> version of the EC key is created
    And the key is deleted
    When the key is recovered
    Then the key URL contains the vault url and <keyName>

    Examples:
      | versionsCount | hsm     | keyName          | curveName |
      | 5             | without | recoverEcKey256  | P-256     |
      | 6             | without | recoverEcKey256k | P-256K    |

  @Key @KeyCreate @KeyDelete @KeyRecover @OCT
  Scenario Outline: OCT_RECOVER_01 Multiple versions of OCT keys are created with the key client then deleted and recovered
    Given a key client is created with the vault named keys-delete
    And an OCT key named <keyName> is prepared with <keySize> bits size
    And <versionsCount> version of the OCT key is created
    And the key is deleted
    When the key is recovered
    Then the key URL contains the vault url and <keyName>

    Examples:
      | versionsCount | keyName          | keySize |
      | 5             | recoverOctKey    | 128     |
      | 6             | recoverOctKey192 | 192     |
