Feature: Key list

  @Key @KeyCreate @KeyList @RSA @CreateVault
  Scenario Outline: RSA_LIST_01 RSA keys are created with the key client then all are listed
    Given a vault is created with name keys-list-rsa-<count>
    And a key client is created with the vault named keys-list-rsa-<count>
    And <count> RSA keys with <keyName>- prefix are created with 2048 bits size without HSM
    When the key properties are listed
    Then the listed keys are matching the ones created

    Examples:
      | count | keyName           |
      | 1     | listRsaKey        |
      | 2     | list-rsa-key-name |

  @Key @KeyCreate @KeyList @EC @CreateVault
  Scenario Outline: EC_LIST_01 EC keys are created with the key client then all are listed
    Given a vault is created with name keys-list-ec-<count>
    And a key client is created with the vault named keys-list-ec-<count>
    And <count> EC keys with <keyName>- prefix are created with P-256 and without HSM
    When the key properties are listed
    Then the listed keys are matching the ones created

    Examples:
      | count | keyName          |
      | 1     | listEcKey        |
      | 2     | list-ec-key-name |
      | 3     | listEcKey        |
      | 5     | list-ec-key-name |
      | 25    | listEcKey        |
      | 42    | list-ec-key-name |

  @Key @KeyCreate @KeyList @OCT @CreateVault
  Scenario Outline: OCT_LIST_01 OCT keys are created with the key client then all are listed
    Given a vault is created with name keys-list-oct-<count>
    And a key client is created with the vault named keys-list-oct-<count>
    And <count> OCT keys with <keyName>- prefix are created with 128 bits size
    When the key properties are listed
    Then the listed keys are matching the ones created

    Examples:
      | count | keyName           |
      | 1     | listOctKey        |
      | 2     | list-oct-key-name |
      | 3     | listOctKey        |
      | 5     | list-oct-key-name |
      | 25    | listOctKey        |
      | 42    | list-oct-key-name |
