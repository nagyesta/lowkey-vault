Feature: Key list deleted

  @Key @KeyCreate @KeyListDeleted @RSA @CreateVault
  Scenario Outline: RSA_LIST_DELETED_01 RSA keys are created and deleted with the key client then all are listed as deleted keys
    Given key API version <api> is used
    And a vault is created with name keys-del-rsa-<index>
    And a key client is created with the vault named keys-del-rsa-<index>
    And <count> RSA keys with <keyName>- prefix are created with 2048 bits size without HSM
    And <count> keys with <keyName>- prefix are deleted
    When the deleted key properties are listed
    Then the listed deleted keys are matching the ones deleted before

    Examples:
      | api | index | count | keyName           |
      | 7.2 | 01    | 1     | listRsaKey        |
      | 7.2 | 02    | 2     | list-rsa-key-name |
      | 7.3 | 03    | 1     | listRsaKey        |
      | 7.3 | 04    | 2     | list-rsa-key-name |

  @Key @KeyCreate @KeyListDeleted @EC @CreateVault
  Scenario Outline: EC_LIST_DELETED_01 EC keys are created and deleted with the key client then all are listed as deleted keys
    Given key API version <api> is used
    And a vault is created with name keys-del-ec-<index>
    And a key client is created with the vault named keys-del-ec-<index>
    And <count> EC keys with <keyName>- prefix are created with P-256 and without HSM
    And <count> keys with <keyName>- prefix are deleted
    When the deleted key properties are listed
    Then the listed deleted keys are matching the ones deleted before

    Examples:
      | api | index | count | keyName          |
      | 7.2 | 01    | 1     | listEcKey        |
      | 7.2 | 02    | 2     | list-ec-key-name |
      | 7.2 | 03    | 3     | listEcKey        |
      | 7.2 | 04    | 5     | list-ec-key-name |
      | 7.2 | 05    | 25    | listEcKey        |
      | 7.2 | 06    | 42    | list-ec-key-name |
      | 7.3 | 07    | 1     | listEcKey        |
      | 7.3 | 08    | 2     | list-ec-key-name |
      | 7.3 | 09    | 3     | listEcKey        |
      | 7.3 | 10    | 5     | list-ec-key-name |
      | 7.3 | 11    | 25    | listEcKey        |
      | 7.3 | 12    | 42    | list-ec-key-name |

  @Key @KeyCreate @KeyListDeleted @OCT @CreateVault
  Scenario Outline: OCT_LIST_DELETED_01 OCT keys are created and deleted with the key client then all are listed as deleted keys
    Given key API version <api> is used
    And a vault is created with name keys-del-oct-<index>
    And a key client is created with the vault named keys-del-oct-<index>
    And <count> OCT keys with <keyName>- prefix are created with 128 bits size
    And <count> keys with <keyName>- prefix are deleted
    When the deleted key properties are listed
    Then the listed deleted keys are matching the ones deleted before

    Examples:
      | api | index | count | keyName           |
      | 7.2 | 01    | 1     | listOctKey        |
      | 7.2 | 02    | 2     | list-oct-key-name |
      | 7.2 | 03    | 3     | listOctKey        |
      | 7.2 | 04    | 5     | list-oct-key-name |
      | 7.2 | 05    | 25    | listOctKey        |
      | 7.2 | 06    | 42    | list-oct-key-name |
      | 7.3 | 07    | 1     | listOctKey        |
      | 7.3 | 08    | 2     | list-oct-key-name |
      | 7.3 | 09    | 3     | listOctKey        |
      | 7.3 | 10    | 5     | list-oct-key-name |
      | 7.3 | 11    | 25    | listOctKey        |
      | 7.3 | 12    | 42    | list-oct-key-name |
