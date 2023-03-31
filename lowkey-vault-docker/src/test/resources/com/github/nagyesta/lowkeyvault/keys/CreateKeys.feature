Feature: Key creation

    @Key @KeyCreate @RSA
    Scenario Outline: RSA_CREATE_01 Single versions of RSA keys can be created with the key client
        Given key API version <api> is used
        And a key client is created with the vault named keys-generic
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
            | api | hsm     | keyName                     | keySize | nBytes | enabledStatus | operations                                           | expires | notBefore | tagMap            |
            | 7.2 | without | 72-createRsaKey             | 2048    | 257    | enabled       | null                                                 | null    | null      | null              |
            | 7.2 | without | 72-createRsaKey4096         | 4096    | 513    | enabled       | null                                                 | null    | null      | null              |
            | 7.2 | without | 72-create-rsa-key-name      | 2048    | 257    | enabled       | null                                                 | null    | null      | null              |
            | 7.2 | without | 72-create-rsa-key-name-4096 | 4096    | 513    | enabled       | null                                                 | null    | null      | null              |
            | 7.2 | with    | 72-createRsaHsmKey          | 2048    | 257    | enabled       | null                                                 | null    | null      | null              |
            | 7.2 | with    | 72-createRsaHsmKey4096      | 4096    | 513    | enabled       | null                                                 | null    | null      | null              |
            | 7.2 | with    | 72-create-rsa-hsm-key-name  | 2048    | 257    | enabled       | null                                                 | null    | null      | null              |
            | 7.2 | with    | 72-create-rsa-hsm-key-4096  | 4096    | 513    | enabled       | null                                                 | null    | null      | null              |
            | 7.2 | without | 72-createRsaKeyMap1         | 2048    | 257    | enabled       | null                                                 | null    | null      | aKey:aValue,b1:b2 |
            | 7.2 | without | 72-createRsaKeyMap2         | 2048    | 257    | enabled       | null                                                 | null    | null      | aKey:aValue       |
            | 7.2 | without | 72-createRsaKeyAllOps       | 2048    | 257    | enabled       | encrypt,decrypt,wrapKey,unwrapKey,sign,verify,import | null    | null      | null              |
            | 7.2 | without | 72-createRsaKeyOperations   | 2048    | 257    | enabled       | wrapKey,unwrapKey                                    | null    | null      | null              |
            | 7.2 | without | 72-createRsaKeyDates        | 2048    | 257    | enabled       | null                                                 | 4321    | 1234      | null              |
            | 7.2 | without | 72-createRsaKeyNotEnabled   | 2048    | 257    | not enabled   | null                                                 | null    | null      | null              |
            | 7.3 | without | 73-createRsaKey             | 2048    | 257    | enabled       | null                                                 | null    | null      | null              |
            | 7.3 | without | 73-createRsaKey4096         | 4096    | 513    | enabled       | null                                                 | null    | null      | null              |
            | 7.3 | without | 73-create-rsa-key-name      | 2048    | 257    | enabled       | null                                                 | null    | null      | null              |
            | 7.3 | without | 73-create-rsa-key-name-4096 | 4096    | 513    | enabled       | null                                                 | null    | null      | null              |
            | 7.3 | with    | 73-createRsaHsmKey          | 2048    | 257    | enabled       | null                                                 | null    | null      | null              |
            | 7.3 | with    | 73-createRsaHsmKey4096      | 4096    | 513    | enabled       | null                                                 | null    | null      | null              |
            | 7.3 | with    | 73-create-rsa-hsm-key-name  | 2048    | 257    | enabled       | null                                                 | null    | null      | null              |
            | 7.3 | with    | 73-create-rsa-hsm-key-4096  | 4096    | 513    | enabled       | null                                                 | null    | null      | null              |
            | 7.3 | without | 73-createRsaKeyMap1         | 2048    | 257    | enabled       | null                                                 | null    | null      | aKey:aValue,b1:b2 |
            | 7.3 | without | 73-createRsaKeyMap2         | 2048    | 257    | enabled       | null                                                 | null    | null      | aKey:aValue       |
            | 7.3 | without | 73-createRsaKeyAllOps       | 2048    | 257    | enabled       | encrypt,decrypt,wrapKey,unwrapKey,sign,verify,import | null    | null      | null              |
            | 7.3 | without | 73-createRsaKeyOperations   | 2048    | 257    | enabled       | wrapKey,unwrapKey                                    | null    | null      | null              |
            | 7.3 | without | 73-createRsaKeyDates        | 2048    | 257    | enabled       | null                                                 | 4321    | 1234      | null              |
            | 7.3 | without | 73-createRsaKeyNotEnabled   | 2048    | 257    | not enabled   | null                                                 | null    | null      | null              |

    @Key @KeyCreate @EC
    Scenario Outline: EC_CREATE_01 Single versions of EC keys can be created with the key client
        Given key API version <api> is used
        And a key client is created with the vault named keys-generic
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
            | api | hsm     | keyName                  | curveName | nBytes | enabledStatus | operations         | expires | notBefore | tagMap            |
            | 7.2 | without | 72-createEcKey256        | P-256     | 32     | enabled       | null               | null    | null      | null              |
            | 7.2 | without | 72-createEcKey256k       | P-256K    | 32     | enabled       | null               | null    | null      | null              |
            | 7.2 | without | 72-createEcKey384        | P-384     | 48     | enabled       | null               | null    | null      | null              |
            | 7.2 | without | 72-createEcKey521        | P-521     | 65     | enabled       | null               | null    | null      | null              |
            | 7.2 | with    | 72-createEcKey256Hsm     | P-256     | 32     | enabled       | null               | null    | null      | null              |
            | 7.2 | with    | 72-createEcKey256kHsm    | P-256K    | 32     | enabled       | null               | null    | null      | null              |
            | 7.2 | with    | 72-createEcKey384Hsm     | P-384     | 48     | enabled       | null               | null    | null      | null              |
            | 7.2 | with    | 72-createEcKey521Hsm     | P-521     | 65     | enabled       | null               | null    | null      | null              |
            | 7.2 | without | 72-createEcKeyMap1       | P-256     | 32     | enabled       | null               | null    | null      | aKey:aValue,b1:b2 |
            | 7.2 | without | 72-createEcKeyMap2       | P-256     | 32     | enabled       | null               | null    | null      | aKey:aValue       |
            | 7.2 | without | 72-createEcKeyAllOps     | P-256     | 32     | enabled       | sign,verify,import | null    | null      | null              |
            | 7.2 | without | 72-createEcKeyOperations | P-256     | 32     | enabled       | sign,verify        | null    | null      | null              |
            | 7.2 | without | 72-createEcKeyDates      | P-256     | 32     | enabled       | null               | 4321    | 1234      | null              |
            | 7.2 | without | 72-createEcKeyNotEnabled | P-256     | 32     | not enabled   | null               | null    | null      | null              |
            | 7.3 | without | 73-createEcKey256        | P-256     | 32     | enabled       | null               | null    | null      | null              |
            | 7.3 | without | 73-createEcKey256k       | P-256K    | 32     | enabled       | null               | null    | null      | null              |
            | 7.3 | without | 73-createEcKey384        | P-384     | 48     | enabled       | null               | null    | null      | null              |
            | 7.3 | without | 73-createEcKey521        | P-521     | 65     | enabled       | null               | null    | null      | null              |
            | 7.3 | with    | 73-createEcKey256Hsm     | P-256     | 32     | enabled       | null               | null    | null      | null              |
            | 7.3 | with    | 73-createEcKey256kHsm    | P-256K    | 32     | enabled       | null               | null    | null      | null              |
            | 7.3 | with    | 73-createEcKey384Hsm     | P-384     | 48     | enabled       | null               | null    | null      | null              |
            | 7.3 | with    | 73-createEcKey521Hsm     | P-521     | 65     | enabled       | null               | null    | null      | null              |
            | 7.3 | without | 73-createEcKeyMap1       | P-256     | 32     | enabled       | null               | null    | null      | aKey:aValue,b1:b2 |
            | 7.3 | without | 73-createEcKeyMap2       | P-256     | 32     | enabled       | null               | null    | null      | aKey:aValue       |
            | 7.3 | without | 73-createEcKeyAllOps     | P-256     | 32     | enabled       | sign,verify,import | null    | null      | null              |
            | 7.3 | without | 73-createEcKeyOperations | P-256     | 32     | enabled       | sign,verify        | null    | null      | null              |
            | 7.3 | without | 73-createEcKeyDates      | P-256     | 32     | enabled       | null               | 4321    | 1234      | null              |
            | 7.3 | without | 73-createEcKeyNotEnabled | P-256     | 32     | not enabled   | null               | null    | null      | null              |

    @Key @KeyCreate @OCT
    Scenario Outline: OCT_CREATE_01 Single versions of OCT keys can be created with the key client
        Given key API version <api> is used
        And a key client is created with the vault named keys-generic
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
            | api | hsm  | keyName                   | keySize | enabledStatus | operations                               | expires | notBefore | tagMap            |
            | 7.2 | with | 72-createOctKey           | 128     | enabled       | null                                     | null    | null      | null              |
            | 7.2 | with | 72-createOctKey192        | 192     | enabled       | null                                     | null    | null      | null              |
            | 7.2 | with | 72-createOctKey256        | 256     | enabled       | null                                     | null    | null      | null              |
            | 7.2 | with | 72-create-oct-key-128     | 128     | enabled       | null                                     | null    | null      | null              |
            | 7.2 | with | 72-createOctKeyMap1       | 128     | enabled       | null                                     | null    | null      | aKey:aValue,b1:b2 |
            | 7.2 | with | 72-createOctKeyMap2       | 128     | enabled       | null                                     | null    | null      | aKey:aValue       |
            | 7.2 | with | 72-createOctKeyAllOps     | 128     | enabled       | encrypt,decrypt,wrapKey,unwrapKey,import | null    | null      | null              |
            | 7.2 | with | 72-createOctKeyOperations | 128     | enabled       | wrapKey,unwrapKey                        | null    | null      | null              |
            | 7.2 | with | 72-createOctKeyDates      | 128     | enabled       | null                                     | 4321    | 1234      | null              |
            | 7.2 | with | 72-createOctKeyNotEnabled | 128     | not enabled   | null                                     | null    | null      | null              |
            | 7.3 | with | 73-createOctKey           | 128     | enabled       | null                                     | null    | null      | null              |
            | 7.3 | with | 73-createOctKey192        | 192     | enabled       | null                                     | null    | null      | null              |
            | 7.3 | with | 73-createOctKey256        | 256     | enabled       | null                                     | null    | null      | null              |
            | 7.3 | with | 73-create-oct-key-128     | 128     | enabled       | null                                     | null    | null      | null              |
            | 7.3 | with | 73-createOctKeyMap1       | 128     | enabled       | null                                     | null    | null      | aKey:aValue,b1:b2 |
            | 7.3 | with | 73-createOctKeyMap2       | 128     | enabled       | null                                     | null    | null      | aKey:aValue       |
            | 7.3 | with | 73-createOctKeyAllOps     | 128     | enabled       | encrypt,decrypt,wrapKey,unwrapKey,import | null    | null      | null              |
            | 7.3 | with | 73-createOctKeyOperations | 128     | enabled       | wrapKey,unwrapKey                        | null    | null      | null              |
            | 7.3 | with | 73-createOctKeyDates      | 128     | enabled       | null                                     | 4321    | 1234      | null              |
            | 7.3 | with | 73-createOctKeyNotEnabled | 128     | not enabled   | null                                     | null    | null      | null              |
