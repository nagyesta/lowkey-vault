Feature: Key rotation

    @Key @KeyCreate @KeyRotate @RSA
    Scenario Outline: RSA_ROTATE_01 Single versions of RSA keys can be created with the key client, then rotated and result observed
        Given key API version <api> is used
        And a key client is created with the vault named keys-generic
        And an RSA key named <keyName> is prepared with <keySize> bits size <hsm> HSM
        And the key is set to expire <expires> seconds after creation
        And the key is set to be not usable until <notBefore> seconds after creation
        And the key is set to use <tagMap> as tags
        And the key has <operations> operations granted
        And the key is set to be <enabledStatus>
        And the RSA key is created
        When the key named <keyName> is rotated
        Then the created key is using RSA algorithm with <nBytes> bytes length
        And the key name is <keyName>
        And the key URL contains the vault url and <keyName>
        And the key enabled status is enabled
        And the key has <operations> as operations
        And the key has <tagMap> as tags
        And the key was created <hsm> HSM
        And the EC specific fields are not populated
        And the OCT specific fields are not populated
        And the key recovery settings are default

        Examples:
            | api | hsm     | keyName                   | keySize | nBytes | enabledStatus | operations                                           | expires | notBefore | tagMap            |
            | 7.3 | without | 73-rotateRsaKey           | 2048    | 257    | enabled       | null                                                 | null    | null      | null              |
            | 7.3 | without | 73-rotateRsaKeyAllOps     | 4096    | 513    | enabled       | encrypt,decrypt,wrapKey,unwrapKey,sign,verify,import | null    | null      | null              |
            | 7.3 | without | 73-rotateRsaKeyNotEnabled | 2048    | 257    | not enabled   | null                                                 | 4321    | 1234      | null              |
            | 7.3 | without | 73-rotateRsaKeyMap        | 2048    | 257    | enabled       | null                                                 | null    | null      | aKey:aValue,b1:b2 |
            | 7.4 | without | 74-rotateRsaKeyMap        | 2048    | 257    | enabled       | null                                                 | null    | null      | aKey:aValue,b1:b2 |
            | 7.5 | without | 75-rotateRsaKeyMap        | 2048    | 257    | enabled       | null                                                 | null    | null      | aKey:aValue,b1:b2 |
            | 7.6 | without | 76-rotateRsaKeyMap        | 2048    | 257    | enabled       | null                                                 | null    | null      | aKey:aValue,b1:b2 |

    @Key @KeyCreate @KeyRotate @EC
    Scenario Outline: EC_ROTATE_01 Single versions of EC keys can be created with the key client, then rotated and result observed
        Given key API version <api> is used
        And a key client is created with the vault named keys-generic
        And an EC key named <keyName> is prepared with <curveName> and <hsm> HSM
        And the key is set to expire <expires> seconds after creation
        And the key is set to be not usable until <notBefore> seconds after creation
        And the key is set to use <tagMap> as tags
        And the key has <operations> operations granted
        And the key is set to be <enabledStatus>
        And the EC key is created
        When the key named <keyName> is rotated
        Then the created key is using EC algorithm with <curveName> curve name and <nBytes> bytes length
        And the key name is <keyName>
        And the key URL contains the vault url and <keyName>
        And the key enabled status is enabled
        And the key has <operations> as operations
        And the key has <tagMap> as tags
        And the key was created <hsm> HSM
        And the RSA specific fields are not populated
        And the OCT specific fields are not populated
        And the key recovery settings are default

        Examples:
            | api | hsm     | keyName                  | curveName | nBytes | enabledStatus | operations         | expires | notBefore | tagMap            |
            | 7.3 | without | 73-rotateEcKey256        | P-256     | 32     | enabled       | null               | null    | null      | null              |
            | 7.3 | without | 73-rotateEcKeyMap1       | P-256     | 32     | enabled       | null               | null    | null      | aKey:aValue,b1:b2 |
            | 7.3 | without | 73-rotateEcKeyAllOps     | P-256     | 32     | enabled       | sign,verify,import | null    | null      | null              |
            | 7.3 | without | 73-rotateEcKeyNotEnabled | P-256     | 32     | not enabled   | null               | null    | null      | null              |
            | 7.4 | without | 74-rotateEcKeyNotEnabled | P-256     | 32     | not enabled   | null               | null    | null      | null              |
            | 7.5 | without | 75-rotateEcKeyNotEnabled | P-256     | 32     | not enabled   | null               | null    | null      | null              |
            | 7.6 | without | 76-rotateEcKeyNotEnabled | P-256     | 32     | not enabled   | null               | null    | null      | null              |

    @Key @KeyCreate @KeyRotate @OCT
    Scenario Outline: OCT_ROTATE_01 Single versions of OCT keys can be created with the key client, then rotated and result observed
        Given key API version <api> is used
        And a key client is created with the vault named keys-generic
        And an OCT key named <keyName> is prepared with <keySize> bits size
        And the key is set to expire <expires> seconds after creation
        And the key is set to be not usable until <notBefore> seconds after creation
        And the key is set to use <tagMap> as tags
        And the key has <operations> operations granted
        And the key is set to be <enabledStatus>
        And the OCT key is created
        When the key named <keyName> is rotated
        Then the created key is using OCT algorithm
        And the key name is <keyName>
        And the key URL contains the vault url and <keyName>
        And the key enabled status is enabled
        And the key has <operations> as operations
        And the key has <tagMap> as tags
        And the key was created <hsm> HSM
        And the RSA specific fields are not populated
        And the EC specific fields are not populated
        And the OCT specific fields are not populated
        And the key recovery settings are default

        Examples:
            | api | hsm  | keyName                   | keySize | enabledStatus | operations                               | expires | notBefore | tagMap            |
            | 7.3 | with | 73-rotateOctKey           | 128     | enabled       | null                                     | null    | null      | null              |
            | 7.3 | with | 73-rotateOctKeyMap1       | 128     | enabled       | null                                     | null    | null      | aKey:aValue,b1:b2 |
            | 7.3 | with | 73-rotateOctKeyAllOps     | 128     | enabled       | encrypt,decrypt,wrapKey,unwrapKey,import | null    | null      | null              |
            | 7.3 | with | 73-rotateOctKeyNotEnabled | 128     | not enabled   | null                                     | null    | null      | null              |
            | 7.4 | with | 74-rotateOctKey           | 128     | enabled       | null                                     | null    | null      | null              |
            | 7.5 | with | 75-rotateOctKey           | 128     | enabled       | null                                     | null    | null      | null              |
            | 7.6 | with | 76-rotateOctKey           | 128     | enabled       | null                                     | null    | null      | null              |
