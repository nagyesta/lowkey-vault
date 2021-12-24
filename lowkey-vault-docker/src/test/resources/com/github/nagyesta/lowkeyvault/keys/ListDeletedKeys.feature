Feature: Key list deleted

  @Key @KeyCreate @KeyListDeleted @RSA
  Scenario Outline: RSA_LIST_DELETED_01 RSA keys are created and deleted with the key client then all are listed as deleted keys
    Given a key client is created with the vault named keys-del-rsa-<count>
    And <count> RSA keys with <keyName>- prefix are created with 2048 bits size without HSM
    And <count> keys with <keyName>- prefix are deleted
    When the deleted key properties are listed
    Then the listed deleted keys are matching the ones deleted before

    Examples:
      | count | keyName           |
      | 1     | listRsaKey        |
      | 2     | list-rsa-key-name |

  @Key @KeyCreate @KeyListDeleted @EC
  Scenario Outline: EC_LIST_DELETED_01 EC keys are created and deleted with the key client then all are listed as deleted keys
    Given a key client is created with the vault named keys-del-ec-<count>
    And <count> EC keys with <keyName>- prefix are created with P-256 and without HSM
    And <count> keys with <keyName>- prefix are deleted
    When the deleted key properties are listed
    Then the listed deleted keys are matching the ones deleted before

    Examples:
      | count | keyName          |
      | 1     | listEcKey        |
      | 2     | list-ec-key-name |
      | 3     | listEcKey        |
      | 5     | list-ec-key-name |
      | 25    | listEcKey        |
      | 42    | list-ec-key-name |

  @Key @KeyCreate @KeyListDeleted @OCT
  Scenario Outline: OCT_LIST_DELETED_01 OCT keys are created and deleted with the key client then all are listed as deleted keys
    Given a key client is created with the vault named keys-del-oct-<count>
    And <count> OCT keys with <keyName>- prefix are created with 128 bits size
    And <count> keys with <keyName>- prefix are deleted
    When the deleted key properties are listed
    Then the listed deleted keys are matching the ones deleted before

    Examples:
      | count | keyName           |
      | 1     | listOctKey        |
      | 2     | list-oct-key-name |
      | 3     | listOctKey        |
      | 5     | list-oct-key-name |
      | 25    | listOctKey        |
      | 42    | list-oct-key-name |
