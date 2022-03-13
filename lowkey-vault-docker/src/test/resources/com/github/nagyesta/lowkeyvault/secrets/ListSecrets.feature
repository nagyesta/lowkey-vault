Feature: Secret list

  @Secret @SecretCreate @SecretList @CreateVault
  Scenario Outline: SECRET_LIST_01 Secrets are created with the secret client then all are listed
    Given a vault is created with name secrets-list-<count>
    And a secret client is created with the vault named secrets-list-<count>
    And <count> secrets with <secretName>- prefix are created valued abc123
    When the secret properties are listed
    Then the listed secrets are matching the ones created

    Examples:
      | count | secretName       |
      | 1     | listSecret       |
      | 2     | list-secret-name |
      | 3     | listSecret       |
      | 5     | list-secret-name |
      | 25    | listSecret       |
      | 42    | list-secret-name |
