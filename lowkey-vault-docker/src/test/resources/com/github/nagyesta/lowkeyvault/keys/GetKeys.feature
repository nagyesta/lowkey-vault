Feature: Key get

  @Key @KeyCreate @KeyGet @RSA
  Scenario Outline: RSA_GET_01 Multiple versions of RSA keys are created with the key client then the latest is fetched
    Given a key client is created with the vault named keys-generic
    And an RSA key named <keyName> is prepared with <keySize> bits size <hsm> HSM
    And <versionsCount> version of the RSA key is created
    And the key is set to expire <expires> seconds after creation
    And the key is set to be not usable until <notBefore> seconds after creation
    And the key is set to use <tagMap> as tags
    And the key has <operations> operations granted
    And the key is set to be <enabledStatus>
    When the RSA key is created
    And the last key version of <keyName> is fetched without providing a version
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
      | versionsCount | hsm     | keyName                 | keySize | nBytes | enabledStatus | operations                                           | expires | notBefore | tagMap            |
      | 2             | without | get01RsaKey             | 2048    | 257    | enabled       | null                                                 | null    | null      | null              |
      | 1             | without | get01RsaKey4096         | 4096    | 513    | enabled       | null                                                 | null    | null      | null              |
      | 2             | without | get01-rsa-key-name      | 2048    | 257    | enabled       | null                                                 | null    | null      | null              |
      | 1             | without | get01-rsa-key-name-4096 | 4096    | 513    | enabled       | null                                                 | null    | null      | null              |
      | 4             | with    | get01RsaHsmKey          | 2048    | 257    | enabled       | null                                                 | null    | null      | null              |
      | 3             | with    | get01RsaHsmKey4096      | 4096    | 513    | enabled       | null                                                 | null    | null      | null              |
      | 4             | with    | get01-rsa-hsm-key-name  | 2048    | 257    | enabled       | null                                                 | null    | null      | null              |
      | 3             | with    | get01-rsa-hsm-key-4096  | 4096    | 513    | enabled       | null                                                 | null    | null      | null              |
      | 4             | without | get01RsaKeyMap1         | 2048    | 257    | enabled       | null                                                 | null    | null      | aKey:aValue,b1:b2 |
      | 3             | without | get01RsaKeyMap2         | 2048    | 257    | enabled       | null                                                 | null    | null      | aKey:aValue       |
      | 4             | without | get01RsaKeyAllOps       | 2048    | 257    | enabled       | encrypt,decrypt,wrapKey,unwrapKey,sign,verify,import | null    | null      | null              |
      | 3             | without | get01RsaKeyOperations   | 2048    | 257    | enabled       | wrapKey,unwrapKey                                    | null    | null      | null              |
      | 4             | without | get01RsaKeyDates        | 2048    | 257    | enabled       | null                                                 | 4321    | 1234      | null              |
      | 3             | without | get01RsaKeyNotEnabled   | 2048    | 257    | not enabled   | null                                                 | null    | null      | null              |

  @Key @KeyCreate @KeyGet @EC
  Scenario Outline: EC_GET_01 Multiple versions of EC keys are created with the key client then the latest is fetched
    Given a key client is created with the vault named keys-generic
    And an EC key named <keyName> is prepared with <curveName> and <hsm> HSM
    And <versionsCount> version of the EC key is created
    And the key is set to expire <expires> seconds after creation
    And the key is set to be not usable until <notBefore> seconds after creation
    And the key is set to use <tagMap> as tags
    And the key has <operations> operations granted
    And the key is set to be <enabledStatus>
    When the EC key is created
    And the last key version of <keyName> is fetched without providing a version
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
      | versionsCount | hsm     | keyName              | curveName | nBytes | enabledStatus | operations                                           | expires | notBefore | tagMap            |
      | 2             | without | get01EcKey256        | P-256     | 32     | enabled       | null                                                 | null    | null      | null              |
      | 1             | without | get01EcKey256k       | P-256K    | 32     | enabled       | null                                                 | null    | null      | null              |
      | 2             | without | get01EcKey384        | P-384     | 48     | enabled       | null                                                 | null    | null      | null              |
      | 1             | without | get01EcKey521        | P-521     | 65     | enabled       | null                                                 | null    | null      | null              |
      | 4             | with    | get01EcKey256Hsm     | P-256     | 32     | enabled       | null                                                 | null    | null      | null              |
      | 3             | with    | get01EcKey256kHsm    | P-256K    | 32     | enabled       | null                                                 | null    | null      | null              |
      | 4             | with    | get01EcKey384Hsm     | P-384     | 48     | enabled       | null                                                 | null    | null      | null              |
      | 3             | with    | get01EcKey521Hsm     | P-521     | 65     | enabled       | null                                                 | null    | null      | null              |
      | 4             | without | get01EcKeyMap1       | P-256     | 32     | enabled       | null                                                 | null    | null      | aKey:aValue,b1:b2 |
      | 3             | without | get01EcKeyMap2       | P-256     | 32     | enabled       | null                                                 | null    | null      | aKey:aValue       |
      | 4             | without | get01EcKeyAllOps     | P-256     | 32     | enabled       | encrypt,decrypt,wrapKey,unwrapKey,sign,verify,import | null    | null      | null              |
      | 3             | without | get01EcKeyOperations | P-256     | 32     | enabled       | wrapKey,unwrapKey                                    | null    | null      | null              |
      | 4             | without | get01EcKeyDates      | P-256     | 32     | enabled       | null                                                 | 4321    | 1234      | null              |
      | 3             | without | get01EcKeyNotEnabled | P-256     | 32     | not enabled   | null                                                 | null    | null      | null              |

  @Key @KeyCreate @KeyGet @OCT
  Scenario Outline: OCT_GET_01 Multiple versions of OCT keys are created with the key client then the latest is fetched
    Given a key client is created with the vault named keys-generic
    And an OCT key named <keyName> is prepared with <keySize> bits size
    And <versionsCount> version of the OCT key is created
    And the key is set to expire <expires> seconds after creation
    And the key is set to be not usable until <notBefore> seconds after creation
    And the key is set to use <tagMap> as tags
    And the key has <operations> operations granted
    And the key is set to be <enabledStatus>
    When the OCT key is created
    And the last key version of <keyName> is fetched without providing a version
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
      | versionsCount | hsm  | keyName               | keySize | enabledStatus | operations                                           | expires | notBefore | tagMap            |
      | 2             | with | get01OctKey           | 128     | enabled       | null                                                 | null    | null      | null              |
      | 1             | with | get01OctKey192        | 192     | enabled       | null                                                 | null    | null      | null              |
      | 2             | with | get01OctKey256        | 256     | enabled       | null                                                 | null    | null      | null              |
      | 1             | with | get01-oct-key-128     | 128     | enabled       | null                                                 | null    | null      | null              |
      | 4             | with | get01OctKeyMap1       | 128     | enabled       | null                                                 | null    | null      | aKey:aValue,b1:b2 |
      | 3             | with | get01OctKeyMap2       | 128     | enabled       | null                                                 | null    | null      | aKey:aValue       |
      | 4             | with | get01OctKeyAllOps     | 128     | enabled       | encrypt,decrypt,wrapKey,unwrapKey,sign,verify,import | null    | null      | null              |
      | 3             | with | get01OctKeyOperations | 128     | enabled       | wrapKey,unwrapKey                                    | null    | null      | null              |
      | 4             | with | get01OctKeyDates      | 128     | enabled       | null                                                 | 4321    | 1234      | null              |
      | 3             | with | get01OctKeyNotEnabled | 128     | not enabled   | null                                                 | null    | null      | null              |

  @Key @KeyCreate @KeyGet @RSA
  Scenario Outline: RSA_GET_02 Multiple versions of RSA keys are created with the key client then the first is fetched by version
    Given a key client is created with the vault named keys-generic
    And an RSA key named <keyName> is prepared with <keySize> bits size <hsm> HSM
    And <versionsCount> version of the RSA key is created
    When the RSA key is created
    And the first key version of <keyName> is fetched with providing a version
    Then the created key is using RSA algorithm with <nBytes> bytes length
    And the key name is <keyName>
    And the key URL contains the vault url and <keyName>
    And the key was created <hsm> HSM
    And the EC specific fields are not populated
    And the OCT specific fields are not populated
    And the key recovery settings are default

    Examples:
      | versionsCount | hsm     | keyName            | keySize | nBytes |
      | 5             | without | get02RsaKey        | 2048    | 257    |
      | 6             | without | get02-rsa-key-name | 2048    | 257    |

  @Key @KeyCreate @KeyGet @EC
  Scenario Outline: EC_GET_02 Multiple versions of EC keys are created with the key client then the first is fetched by version
    Given a key client is created with the vault named keys-generic
    And an EC key named <keyName> is prepared with <curveName> and <hsm> HSM
    And <versionsCount> version of the EC key is created
    When the EC key is created
    And the first key version of <keyName> is fetched with providing a version
    Then the created key is using EC algorithm with <curveName> curve name and <nBytes> bytes length
    And the key name is <keyName>
    And the key URL contains the vault url and <keyName>
    And the key was created <hsm> HSM
    And the RSA specific fields are not populated
    And the OCT specific fields are not populated
    And the key recovery settings are default

    Examples:
      | versionsCount | hsm     | keyName        | curveName | nBytes |
      | 5             | without | get02EcKey256  | P-256     | 32     |
      | 6             | without | get02EcKey256k | P-256K    | 32     |

  @Key @KeyCreate @KeyGet @OCT
  Scenario Outline: OCT_GET_02 Multiple versions of OCT keys are created with the key client then the first is fetched by version
    Given a key client is created with the vault named keys-generic
    And an OCT key named <keyName> is prepared with <keySize> bits size
    And <versionsCount> version of the OCT key is created
    When the OCT key is created
    And the first key version of <keyName> is fetched with providing a version
    Then the created key is using OCT algorithm
    And the key name is <keyName>
    And the key URL contains the vault url and <keyName>
    And the key was created <hsm> HSM
    And the RSA specific fields are not populated
    And the EC specific fields are not populated
    And the OCT specific fields are not populated
    And the key recovery settings are default

    Examples:
      | versionsCount | hsm  | keyName        | keySize |
      | 5             | with | get02OctKey    | 128     |
      | 6             | with | get02OctKey192 | 192     |

  @Key @KeyCreate @KeyGet @KeyUpdate @EC
  Scenario Outline: EC_UPDATE_01 Multiple versions of EC keys are created with the key client then the latest is updated and fetched
    Given a key client is created with the vault named keys-generic
    And an EC key named <keyName> is prepared with <curveName> and <hsm> HSM
    And <versionsCount> version of the EC key is created
    When the last version of the key is prepared for an update
    And the key is updated to expire <expires> seconds after creation
    And the key is updated to be not usable until <notBefore> seconds after creation
    And the key is updated to use <tagMap> as tags
    And the key is updated to have <operations> operations granted
    And the key is updated to be <enabledStatus>
    When the key update request is sent
    And the last key version of <keyName> is fetched without providing a version
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
      | versionsCount | hsm     | keyName                 | curveName | nBytes | enabledStatus | operations                                           | expires | notBefore | tagMap            |
      | 2             | without | update01EcKey256        | P-256     | 32     | enabled       | null                                                 | null    | null      | null              |
      | 1             | without | update01EcKey256k       | P-256K    | 32     | enabled       | null                                                 | null    | null      | null              |
      | 2             | without | update01EcKey384        | P-384     | 48     | enabled       | null                                                 | null    | null      | null              |
      | 1             | without | update01EcKey521        | P-521     | 65     | enabled       | null                                                 | null    | null      | null              |
      | 4             | with    | update01EcKey256Hsm     | P-256     | 32     | enabled       | null                                                 | null    | null      | null              |
      | 3             | with    | update01EcKey256kHsm    | P-256K    | 32     | enabled       | null                                                 | null    | null      | null              |
      | 4             | with    | update01EcKey384Hsm     | P-384     | 48     | enabled       | null                                                 | null    | null      | null              |
      | 3             | with    | update01EcKey521Hsm     | P-521     | 65     | enabled       | null                                                 | null    | null      | null              |
      | 4             | without | update01EcKeyMap1       | P-256     | 32     | enabled       | null                                                 | null    | null      | aKey:aValue,b1:b2 |
      | 3             | without | update01EcKeyMap2       | P-256     | 32     | enabled       | null                                                 | null    | null      | aKey:aValue       |
      | 4             | without | update01EcKeyAllOps     | P-256     | 32     | enabled       | encrypt,decrypt,wrapKey,unwrapKey,sign,verify,import | null    | null      | null              |
      | 3             | without | update01EcKeyOperations | P-256     | 32     | enabled       | wrapKey,unwrapKey                                    | null    | null      | null              |
      | 4             | without | update01EcKeyDates      | P-256     | 32     | enabled       | null                                                 | 4321    | 1234      | null              |
      | 3             | without | update01EcKeyNotEnabled | P-256     | 32     | not enabled   | null                                                 | null    | null      | null              |
