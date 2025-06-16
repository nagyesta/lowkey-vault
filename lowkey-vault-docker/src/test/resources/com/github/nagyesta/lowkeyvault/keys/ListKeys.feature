Feature: Key list

    @Key @KeyCreate @KeyList @RSA @CreateVault
    Scenario Outline: RSA_LIST_01 RSA keys are created with the key client then all are listed
        Given key API version <api> is used
        And a vault is created with name keys-list-rsa-<index>
        And a key client is created with the vault named keys-list-rsa-<index>
        And <count> RSA keys with <keyName>- prefix are created with 2048 bits size without HSM
        When the key properties are listed
        Then the listed keys are matching the ones created
        And the list of keys should contain 0 managed items

        Examples:
            | api | index | count | keyName           |
            | 7.2 | 01    | 1     | listRsaKey        |
            | 7.3 | 02    | 1     | listRsaKey        |
            | 7.3 | 03    | 2     | list-rsa-key-name |
            | 7.4 | 04    | 2     | list-rsa-key-name |
            | 7.5 | 05    | 2     | list-rsa-key-name |
            | 7.6 | 06    | 2     | list-rsa-key-name |

    @Key @KeyCreate @KeyList @EC @CreateVault
    Scenario Outline: EC_LIST_01 EC keys are created with the key client then all are listed
        Given key API version <api> is used
        And a vault is created with name keys-list-ec-<index>
        And a key client is created with the vault named keys-list-ec-<index>
        And <count> EC keys with <keyName>- prefix are created with P-256 and without HSM
        When the key properties are listed
        Then the listed keys are matching the ones created
        And the list of keys should contain 0 managed items

        Examples:
            | api | index | count | keyName          |
            | 7.2 | 01    | 1     | listEcKey        |
            | 7.3 | 02    | 1     | listEcKey        |
            | 7.3 | 03    | 2     | list-ec-key-name |
            | 7.3 | 04    | 3     | listEcKey        |
            | 7.3 | 05    | 5     | list-ec-key-name |
            | 7.3 | 06    | 25    | listEcKey        |
            | 7.3 | 07    | 42    | list-ec-key-name |
            | 7.4 | 08    | 2     | list-ec-key-name |
            | 7.6 | 10    | 2     | list-ec-key-name |

    @Key @KeyCreate @KeyList @OCT @CreateVault
    Scenario Outline: OCT_LIST_01 OCT keys are created with the key client then all are listed
        Given key API version <api> is used
        And a vault is created with name keys-list-oct-<index>
        And a key client is created with the vault named keys-list-oct-<index>
        And <count> OCT keys with <keyName>- prefix are created with 128 bits size
        When the key properties are listed
        Then the listed keys are matching the ones created
        And the list of keys should contain 0 managed items

        Examples:
            | api | index | count | keyName           |
            | 7.2 | 01    | 1     | listOctKey        |
            | 7.3 | 02    | 1     | listOctKey        |
            | 7.3 | 03    | 2     | list-oct-key-name |
            | 7.3 | 04    | 3     | listOctKey        |
            | 7.3 | 05    | 5     | list-oct-key-name |
            | 7.3 | 06    | 25    | listOctKey        |
            | 7.3 | 07    | 42    | list-oct-key-name |
            | 7.4 | 08    | 2     | list-oct-key-name |
            | 7.5 | 09    | 2     | list-oct-key-name |
