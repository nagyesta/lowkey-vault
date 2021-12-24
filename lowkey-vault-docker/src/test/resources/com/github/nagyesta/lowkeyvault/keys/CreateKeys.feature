Feature: Key creation

  @Key @KeyCreate @RSA
  Scenario Outline: RSA_CREATE_01 Single versions of RSA keys can be created with the key client
    Given a key client is created with the vault named keys-generic
    And an RSA key named <keyName> is prepared with <keySize> bits size <hsm> HSM
    And the key is set to expire <expires> seconds after creation
    And the key is set to be not usable until <notBefore> seconds after creation
    And the key is set to use <tagMap> as tags
    And the key has <operations> operations granted
    And the key is set to be <enabledStatus>
    When the RSA key is created
    Then the created key is using RSA algorithm with <nBytes> bytes length
    And the key name is <keyName>
    And the key URL contains the vault url and <keyName>
    And the key enabled status is <enabledStatus>
    And the key expires <expires> seconds after creation
    And the key is not usable before <notBefore> seconds after creation
    And the key has <operations> as operations
    And the key has <tagMap> as tags
    And the key was created <hsm> HSM
    And the EC specific fields are not populated
    And the OCT specific fields are not populated
    And the key recovery settings are default

    Examples:
      | hsm     | keyName                  | keySize | nBytes | enabledStatus | operations                                           | expires | notBefore | tagMap            |
      | without | createRsaKey             | 2048    | 257    | enabled       | null                                                 | null    | null      | null              |
      | without | createRsaKey4096         | 4096    | 513    | enabled       | null                                                 | null    | null      | null              |
      | without | create-rsa-key-name      | 2048    | 257    | enabled       | null                                                 | null    | null      | null              |
      | without | create-rsa-key-name-4096 | 4096    | 513    | enabled       | null                                                 | null    | null      | null              |
      | with    | createRsaHsmKey          | 2048    | 257    | enabled       | null                                                 | null    | null      | null              |
      | with    | createRsaHsmKey4096      | 4096    | 513    | enabled       | null                                                 | null    | null      | null              |
      | with    | create-rsa-hsm-key-name  | 2048    | 257    | enabled       | null                                                 | null    | null      | null              |
      | with    | create-rsa-hsm-key-4096  | 4096    | 513    | enabled       | null                                                 | null    | null      | null              |
      | without | createRsaKeyMap1         | 2048    | 257    | enabled       | null                                                 | null    | null      | aKey:aValue,b1:b2 |
      | without | createRsaKeyMap2         | 2048    | 257    | enabled       | null                                                 | null    | null      | aKey:aValue       |
      | without | createRsaKeyAllOps       | 2048    | 257    | enabled       | encrypt,decrypt,wrapKey,unwrapKey,sign,verify,import | null    | null      | null              |
      | without | createRsaKeyOperations   | 2048    | 257    | enabled       | wrapKey,unwrapKey                                    | null    | null      | null              |
      | without | createRsaKeyDates        | 2048    | 257    | enabled       | null                                                 | 4321    | 1234      | null              |
      | without | createRsaKeyNotEnabled   | 2048    | 257    | not enabled   | null                                                 | null    | null      | null              |

  @Key @KeyCreate @EC
  Scenario Outline: EC_CREATE_01 Single versions of EC keys can be created with the key client
    Given a key client is created with the vault named keys-generic
    And an EC key named <keyName> is prepared with <curveName> and <hsm> HSM
    And the key is set to expire <expires> seconds after creation
    And the key is set to be not usable until <notBefore> seconds after creation
    And the key is set to use <tagMap> as tags
    And the key has <operations> operations granted
    And the key is set to be <enabledStatus>
    When the EC key is created
    Then the created key is using EC algorithm with <curveName> curve name and <nBytes> bytes length
    And the key name is <keyName>
    And the key URL contains the vault url and <keyName>
    And the key enabled status is <enabledStatus>
    And the key expires <expires> seconds after creation
    And the key is not usable before <notBefore> seconds after creation
    And the key has <operations> as operations
    And the key has <tagMap> as tags
    And the key was created <hsm> HSM
    And the RSA specific fields are not populated
    And the OCT specific fields are not populated
    And the key recovery settings are default

    Examples:
      | hsm     | keyName               | curveName | nBytes | enabledStatus | operations                                           | expires | notBefore | tagMap            |
      | without | createEcKey256        | P-256     | 32     | enabled       | null                                                 | null    | null      | null              |
      | without | createEcKey256k       | P-256K    | 32     | enabled       | null                                                 | null    | null      | null              |
      | without | createEcKey384        | P-384     | 48     | enabled       | null                                                 | null    | null      | null              |
      | without | createEcKey521        | P-521     | 65     | enabled       | null                                                 | null    | null      | null              |
      | with    | createEcKey256Hsm     | P-256     | 32     | enabled       | null                                                 | null    | null      | null              |
      | with    | createEcKey256kHsm    | P-256K    | 32     | enabled       | null                                                 | null    | null      | null              |
      | with    | createEcKey384Hsm     | P-384     | 48     | enabled       | null                                                 | null    | null      | null              |
      | with    | createEcKey521Hsm     | P-521     | 65     | enabled       | null                                                 | null    | null      | null              |
      | without | createEcKeyMap1       | P-256     | 32     | enabled       | null                                                 | null    | null      | aKey:aValue,b1:b2 |
      | without | createEcKeyMap2       | P-256     | 32     | enabled       | null                                                 | null    | null      | aKey:aValue       |
      | without | createEcKeyAllOps     | P-256     | 32     | enabled       | encrypt,decrypt,wrapKey,unwrapKey,sign,verify,import | null    | null      | null              |
      | without | createEcKeyOperations | P-256     | 32     | enabled       | wrapKey,unwrapKey                                    | null    | null      | null              |
      | without | createEcKeyDates      | P-256     | 32     | enabled       | null                                                 | 4321    | 1234      | null              |
      | without | createEcKeyNotEnabled | P-256     | 32     | not enabled   | null                                                 | null    | null      | null              |

  @Key @KeyCreate @OCT
  Scenario Outline: OCT_CREATE_01 Single versions of OCT keys can be created with the key client
    Given a key client is created with the vault named keys-generic
    And an OCT key named <keyName> is prepared with <keySize> bits size
    And the key is set to expire <expires> seconds after creation
    And the key is set to be not usable until <notBefore> seconds after creation
    And the key is set to use <tagMap> as tags
    And the key has <operations> operations granted
    And the key is set to be <enabledStatus>
    When the OCT key is created
    Then the created key is using OCT algorithm
    And the key name is <keyName>
    And the key URL contains the vault url and <keyName>
    And the key enabled status is <enabledStatus>
    And the key expires <expires> seconds after creation
    And the key is not usable before <notBefore> seconds after creation
    And the key has <operations> as operations
    And the key has <tagMap> as tags
    And the key was created <hsm> HSM
    And the RSA specific fields are not populated
    And the EC specific fields are not populated
    And the OCT specific fields are not populated
    And the key recovery settings are default

    Examples:
      | hsm  | keyName                | keySize | enabledStatus | operations                                           | expires | notBefore | tagMap            |
      | with | createOctKey           | 128     | enabled       | null                                                 | null    | null      | null              |
      | with | createOctKey192        | 192     | enabled       | null                                                 | null    | null      | null              |
      | with | createOctKey256        | 256     | enabled       | null                                                 | null    | null      | null              |
      | with | create-oct-key-128     | 128     | enabled       | null                                                 | null    | null      | null              |
      | with | createOctKeyMap1       | 128     | enabled       | null                                                 | null    | null      | aKey:aValue,b1:b2 |
      | with | createOctKeyMap2       | 128     | enabled       | null                                                 | null    | null      | aKey:aValue       |
      | with | createOctKeyAllOps     | 128     | enabled       | encrypt,decrypt,wrapKey,unwrapKey,sign,verify,import | null    | null      | null              |
      | with | createOctKeyOperations | 128     | enabled       | wrapKey,unwrapKey                                    | null    | null      | null              |
      | with | createOctKeyDates      | 128     | enabled       | null                                                 | 4321    | 1234      | null              |
      | with | createOctKeyNotEnabled | 128     | not enabled   | null                                                 | null    | null      | null              |
