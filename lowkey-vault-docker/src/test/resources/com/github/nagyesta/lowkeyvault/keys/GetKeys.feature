Feature: Key get

    @Key @KeyCreate @KeyGet @RSA
    Scenario Outline: RSA_GET_01 Multiple versions of RSA keys are created with the key client then the latest is fetched
        Given key API version <api> is used
        And a key client is created with the vault named keys-generic
        And an RSA key named <keyName> is prepared with <keySize> bits size <hsm> HSM
        And <versionsCount> version of the RSA key is created
        And the key is set to expire <expires> seconds after creation
        And the key is set to be not usable until <notBefore> seconds after creation
        And the key is set to use <tagMap> as tags
        And the key has <operations> operations granted
        And the key is set to be enabled
        When the RSA key is created
        And the last key version of <keyName> is fetched without providing a version
        Then the created key is using RSA algorithm with <nBytes> bytes length
        And the key name is <keyName>
        And the key URL contains the vault url and <keyName>
        And the key enabled status is enabled
        And the key expires <expires> seconds after creation
        And the key is not usable before <notBefore> seconds after creation
        And the key has <operations> as operations
        And the key has <tagMap> as tags
        And the key was created <hsm> HSM
        And the EC specific fields are not populated
        And the OCT specific fields are not populated
        And the key recovery settings are default

        Examples:
            | api | versionsCount | hsm     | keyName                    | keySize | nBytes | operations                                           | expires | notBefore | tagMap            |
            | 7.2 | 2             | without | 72-get01RsaKey             | 2048    | 257    | null                                                 | null    | null      | null              |
            | 7.3 | 2             | without | 73-get01RsaKey             | 2048    | 257    | null                                                 | null    | null      | null              |
            | 7.3 | 1             | without | 73-get01RsaKey4096         | 4096    | 513    | null                                                 | null    | null      | null              |
            | 7.3 | 2             | without | 73-get01-rsa-key-name      | 2048    | 257    | null                                                 | null    | null      | null              |
            | 7.3 | 1             | without | 73-get01-rsa-key-name-4096 | 4096    | 513    | null                                                 | null    | null      | null              |
            | 7.3 | 4             | with    | 73-get01RsaHsmKey          | 2048    | 257    | null                                                 | null    | null      | null              |
            | 7.3 | 3             | with    | 73-get01RsaHsmKey4096      | 4096    | 513    | null                                                 | null    | null      | null              |
            | 7.3 | 4             | with    | 73-get01-rsa-hsm-key-name  | 2048    | 257    | null                                                 | null    | null      | null              |
            | 7.3 | 3             | with    | 73-get01-rsa-hsm-key-4096  | 4096    | 513    | null                                                 | null    | null      | null              |
            | 7.3 | 4             | without | 73-get01RsaKeyMap1         | 2048    | 257    | null                                                 | null    | null      | aKey:aValue,b1:b2 |
            | 7.3 | 3             | without | 73-get01RsaKeyMap2         | 2048    | 257    | null                                                 | null    | null      | aKey:aValue       |
            | 7.3 | 4             | without | 73-get01RsaKeyAllOps       | 2048    | 257    | encrypt,decrypt,wrapKey,unwrapKey,sign,verify,import | null    | null      | null              |
            | 7.3 | 3             | without | 73-get01RsaKeyOperations   | 2048    | 257    | wrapKey,unwrapKey                                    | null    | null      | null              |
            | 7.3 | 4             | without | 73-get01RsaKeyDates        | 2048    | 257    | null                                                 | 4321    | 1234      | null              |
            | 7.4 | 4             | without | 74-get01RsaKeyDates        | 2048    | 257    | null                                                 | 4321    | 1234      | null              |
            | 7.5 | 4             | without | 75-get01RsaKeyDates        | 2048    | 257    | null                                                 | 4321    | 1234      | null              |

    @Key @KeyCreate @KeyGet @EC
    Scenario Outline: EC_GET_01 Multiple versions of EC keys are created with the key client then the latest is fetched
        Given key API version <api> is used
        And a key client is created with the vault named keys-generic
        And an EC key named <keyName> is prepared with <curveName> and <hsm> HSM
        And <versionsCount> version of the EC key is created
        And the key is set to expire <expires> seconds after creation
        And the key is set to be not usable until <notBefore> seconds after creation
        And the key is set to use <tagMap> as tags
        And the key has <operations> operations granted
        And the key is set to be enabled
        When the EC key is created
        And the last key version of <keyName> is fetched without providing a version
        Then the created key is using EC algorithm with <curveName> curve name and <nBytes> bytes length
        And the key name is <keyName>
        And the key URL contains the vault url and <keyName>
        And the key enabled status is enabled
        And the key expires <expires> seconds after creation
        And the key is not usable before <notBefore> seconds after creation
        And the key has <operations> as operations
        And the key has <tagMap> as tags
        And the key was created <hsm> HSM
        And the RSA specific fields are not populated
        And the OCT specific fields are not populated
        And the key recovery settings are default

        Examples:
            | api | versionsCount | hsm     | keyName                 | curveName | nBytes | operations         | expires | notBefore | tagMap            |
            | 7.2 | 2             | without | 72-get01EcKey256        | P-256     | 32     | null               | null    | null      | null              |
            | 7.3 | 2             | without | 73-get01EcKey256        | P-256     | 32     | null               | null    | null      | null              |
            | 7.3 | 1             | without | 73-get01EcKey256k       | P-256K    | 32     | null               | null    | null      | null              |
            | 7.3 | 2             | without | 73-get01EcKey384        | P-384     | 48     | null               | null    | null      | null              |
            | 7.3 | 1             | without | 73-get01EcKey521        | P-521     | 65     | null               | null    | null      | null              |
            | 7.3 | 4             | with    | 73-get01EcKey256Hsm     | P-256     | 32     | null               | null    | null      | null              |
            | 7.3 | 3             | with    | 73-get01EcKey256kHsm    | P-256K    | 32     | null               | null    | null      | null              |
            | 7.3 | 4             | with    | 73-get01EcKey384Hsm     | P-384     | 48     | null               | null    | null      | null              |
            | 7.3 | 3             | with    | 73-get01EcKey521Hsm     | P-521     | 65     | null               | null    | null      | null              |
            | 7.3 | 4             | without | 73-get01EcKeyMap1       | P-256     | 32     | null               | null    | null      | aKey:aValue,b1:b2 |
            | 7.3 | 3             | without | 73-get01EcKeyMap2       | P-256     | 32     | null               | null    | null      | aKey:aValue       |
            | 7.3 | 4             | without | 73-get01EcKeyAllOps     | P-256     | 32     | sign,verify,import | null    | null      | null              |
            | 7.3 | 3             | without | 73-get01EcKeyOperations | P-256     | 32     | sign,verify        | null    | null      | null              |
            | 7.3 | 4             | without | 73-get01EcKeyDates      | P-256     | 32     | null               | 4321    | 1234      | null              |
            | 7.4 | 4             | without | 74-get01EcKeyDates      | P-256     | 32     | null               | 4321    | 1234      | null              |
            | 7.5 | 4             | without | 75-get01EcKeyDates      | P-256     | 32     | null               | 4321    | 1234      | null              |

    @Key @KeyCreate @KeyGet @OCT
    Scenario Outline: OCT_GET_01 Multiple versions of OCT keys are created with the key client then the latest is fetched
        Given key API version <api> is used
        And a key client is created with the vault named keys-generic
        And an OCT key named <keyName> is prepared with <keySize> bits size
        And <versionsCount> version of the OCT key is created
        And the key is set to expire <expires> seconds after creation
        And the key is set to be not usable until <notBefore> seconds after creation
        And the key is set to use <tagMap> as tags
        And the key has <operations> operations granted
        And the key is set to be enabled
        When the OCT key is created
        And the last key version of <keyName> is fetched without providing a version
        Then the created key is using OCT algorithm
        And the key name is <keyName>
        And the key URL contains the vault url and <keyName>
        And the key enabled status is enabled
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
            | api | versionsCount | hsm  | keyName                  | keySize | operations                               | expires | notBefore | tagMap            |
            | 7.2 | 2             | with | 72-get01OctKey           | 128     | null                                     | null    | null      | null              |
            | 7.3 | 2             | with | 73-get01OctKey           | 128     | null                                     | null    | null      | null              |
            | 7.3 | 1             | with | 73-get01OctKey192        | 192     | null                                     | null    | null      | null              |
            | 7.3 | 2             | with | 73-get01OctKey256        | 256     | null                                     | null    | null      | null              |
            | 7.3 | 1             | with | 73-get01-oct-key-128     | 128     | null                                     | null    | null      | null              |
            | 7.3 | 4             | with | 73-get01OctKeyMap1       | 128     | null                                     | null    | null      | aKey:aValue,b1:b2 |
            | 7.3 | 3             | with | 73-get01OctKeyMap2       | 128     | null                                     | null    | null      | aKey:aValue       |
            | 7.3 | 4             | with | 73-get01OctKeyAllOps     | 128     | encrypt,decrypt,wrapKey,unwrapKey,import | null    | null      | null              |
            | 7.3 | 3             | with | 73-get01OctKeyOperations | 128     | wrapKey,unwrapKey                        | null    | null      | null              |
            | 7.3 | 4             | with | 73-get01OctKeyDates      | 128     | null                                     | 4321    | 1234      | null              |
            | 7.4 | 4             | with | 74-get01OctKeyDates      | 128     | null                                     | 4321    | 1234      | null              |
            | 7.5 | 4             | with | 75-get01OctKeyDates      | 128     | null                                     | 4321    | 1234      | null              |


    @Key @KeyCreate @KeyGet @RSA
    Scenario Outline: RSA_GET_02 Multiple versions of RSA keys are created with the key client then the first is fetched by version
        Given key API version <api> is used
        And a key client is created with the vault named keys-generic
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
            | api | versionsCount | hsm     | keyName               | keySize | nBytes |
            | 7.2 | 2             | without | 72-get02RsaKey        | 2048    | 257    |
            | 7.3 | 2             | without | 73-get02RsaKey        | 2048    | 257    |
            | 7.3 | 3             | without | 73-get02-rsa-key-name | 2048    | 257    |
            | 7.4 | 3             | without | 74-get02-rsa-key-name | 2048    | 257    |
            | 7.5 | 3             | without | 75-get02-rsa-key-name | 2048    | 257    |

    @Key @KeyCreate @KeyGet @EC
    Scenario Outline: EC_GET_02 Multiple versions of EC keys are created with the key client then the first is fetched by version
        Given key API version <api> is used
        And a key client is created with the vault named keys-generic
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
            | api | versionsCount | hsm     | keyName           | curveName | nBytes |
            | 7.2 | 5             | without | 72-get02EcKey256  | P-256     | 32     |
            | 7.3 | 5             | without | 73-get02EcKey256  | P-256     | 32     |
            | 7.3 | 6             | without | 73-get02EcKey256k | P-256K    | 32     |
            | 7.4 | 5             | without | 74-get02EcKey256  | P-256     | 32     |
            | 7.5 | 5             | without | 75-get02EcKey256  | P-256     | 32     |

    @Key @KeyCreate @KeyGet @OCT
    Scenario Outline: OCT_GET_02 Multiple versions of OCT keys are created with the key client then the first is fetched by version
        Given key API version <api> is used
        And a key client is created with the vault named keys-generic
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
            | api | versionsCount | hsm  | keyName           | keySize |
            | 7.2 | 5             | with | 72-get02OctKey    | 128     |
            | 7.3 | 5             | with | 73-get02OctKey    | 128     |
            | 7.3 | 6             | with | 73-get02OctKey192 | 192     |
            | 7.4 | 6             | with | 74-get02OctKey192 | 192     |
            | 7.5 | 6             | with | 75-get02OctKey192 | 192     |


    @Key @KeyCreate @KeyGet @RSA
    Scenario Outline: RSA_GET_03 Multiple versions of disabled RSA keys are created with the key client then the latest is fetched
        Given key API version <api> is used
        And a key client is created with the vault named keys-generic
        And an RSA key named <keyName> is prepared with <keySize> bits size <hsm> HSM
        And <versionsCount> version of the RSA key is created
        And the key is set to be not enabled
        When the RSA key is created
        Then the last key version of <keyName> cannot be fetched as it is not enabled

        Examples:
            | api | versionsCount | hsm     | keyName                  | keySize |
            | 7.2 | 5             | without | 72-get03RsaKeyNotEnabled | 2048    |
            | 7.3 | 2             | without | 73-get03RsaKeyNotEnabled | 2048    |
            | 7.4 | 2             | without | 74-get03RsaKeyNotEnabled | 2048    |
            | 7.5 | 2             | without | 75-get03RsaKeyNotEnabled | 2048    |

    @Key @KeyCreate @KeyGet @EC
    Scenario Outline: EC_GET_03 Multiple versions of disabled EC keys are created with the key client then the latest is fetched
        Given key API version <api> is used
        And a key client is created with the vault named keys-generic
        And an EC key named <keyName> is prepared with <curveName> and <hsm> HSM
        And <versionsCount> version of the EC key is created
        And the key is set to be not enabled
        When the EC key is created
        Then the last key version of <keyName> cannot be fetched as it is not enabled

        Examples:
            | api | versionsCount | hsm     | keyName                 | curveName |
            | 7.2 | 3             | without | 72-get03EcKeyNotEnabled | P-256     |
            | 7.3 | 4             | without | 73-get03EcKeyNotEnabled | P-256     |
            | 7.4 | 4             | without | 74-get03EcKeyNotEnabled | P-256     |
            | 7.5 | 4             | without | 75-get03EcKeyNotEnabled | P-256     |

    @Key @KeyCreate @KeyGet @OCT
    Scenario Outline: OCT_GET_03 Multiple versions of disabled OCT keys are created with the key client then the latest is fetched
        Given key API version <api> is used
        And a key client is created with the vault named keys-generic
        And an OCT key named <keyName> is prepared with <keySize> bits size
        And <versionsCount> version of the OCT key is created
        And the key is set to be not enabled
        When the OCT key is created
        Then the last key version of <keyName> cannot be fetched as it is not enabled

        Examples:
            | api | versionsCount | keyName                  | keySize |
            | 7.2 | 2             | 72-get03OctKeyNotEnabled | 128     |
            | 7.3 | 3             | 73-get03OctKeyNotEnabled | 128     |
            | 7.4 | 3             | 74-get03OctKeyNotEnabled | 128     |
            | 7.5 | 3             | 75-get03OctKeyNotEnabled | 128     |

    @Key @KeyCreate @KeyGet @KeyUpdate @EC
    Scenario Outline: EC_UPDATE_01 Multiple versions of EC keys are created with the key client then the latest is updated and fetched
        Given key API version <api> is used
        And a key client is created with the vault named keys-generic
        And an EC key named <keyName> is prepared with <curveName> and <hsm> HSM
        And <versionsCount> version of the EC key is created
        When the last version of the key is prepared for an update
        And the key is updated to expire <expires> seconds after creation
        And the key is updated to be not usable until <notBefore> seconds after creation
        And the key is updated to use <tagMap> as tags
        And the key is updated to have <operations> operations granted
        And the key is updated to be <enabledStatus>
        When the key update request is sent
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
            | api | versionsCount | hsm     | keyName                    | curveName | nBytes | enabledStatus | operations         | expires | notBefore | tagMap            |
            | 7.2 | 2             | without | 72-update01EcKey256        | P-256     | 32     | enabled       | null               | null    | null      | null              |
            | 7.3 | 2             | without | 73-update01EcKey256        | P-256     | 32     | enabled       | null               | null    | null      | null              |
            | 7.3 | 1             | without | 73-update01EcKey256k       | P-256K    | 32     | enabled       | null               | null    | null      | null              |
            | 7.3 | 2             | without | 73-update01EcKey384        | P-384     | 48     | enabled       | null               | null    | null      | null              |
            | 7.3 | 1             | without | 73-update01EcKey521        | P-521     | 65     | enabled       | null               | null    | null      | null              |
            | 7.3 | 4             | with    | 73-update01EcKey256Hsm     | P-256     | 32     | enabled       | null               | null    | null      | null              |
            | 7.3 | 3             | with    | 73-update01EcKey256kHsm    | P-256K    | 32     | enabled       | null               | null    | null      | null              |
            | 7.3 | 4             | with    | 73-update01EcKey384Hsm     | P-384     | 48     | enabled       | null               | null    | null      | null              |
            | 7.3 | 3             | with    | 73-update01EcKey521Hsm     | P-521     | 65     | enabled       | null               | null    | null      | null              |
            | 7.3 | 4             | without | 73-update01EcKeyMap1       | P-256     | 32     | enabled       | null               | null    | null      | aKey:aValue,b1:b2 |
            | 7.3 | 3             | without | 73-update01EcKeyMap2       | P-256     | 32     | enabled       | null               | null    | null      | aKey:aValue       |
            | 7.3 | 4             | without | 73-update01EcKeyAllOps     | P-256     | 32     | enabled       | sign,verify,import | null    | null      | null              |
            | 7.3 | 3             | without | 73-update01EcKeyOperations | P-256     | 32     | enabled       | sign,verify        | null    | null      | null              |
            | 7.3 | 4             | without | 73-update01EcKeyDates      | P-256     | 32     | enabled       | null               | 4321    | 1234      | null              |
            | 7.3 | 3             | without | 73-update01EcKeyNotEnabled | P-256     | 32     | not enabled   | null               | null    | null      | null              |
            | 7.4 | 4             | without | 74-update01EcKeyDates      | P-256     | 32     | enabled       | null               | 4321    | 1234      | null              |
            | 7.5 | 4             | without | 75-update01EcKeyDates      | P-256     | 32     | enabled       | null               | 4321    | 1234      | null              |
