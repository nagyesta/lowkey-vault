Feature: Secret list deleted

  @Secret @SecretCreate @SecretListDeleted @CreateVault
  Scenario Outline: SECRET_LIST_DELETED_01 Secrets are created and deleted with the secret client then all are listed as deleted secrets
    Given a vault is created with name secrets-del-<count>
    And a secret client is created with the vault named secrets-del-<count>
    And <count> secrets with <secretName>- prefix are created valued abc123
    And <count> secrets with <secretName>- prefix are deleted
    When the deleted secret properties are listed
    Then the listed deleted secrets are matching the ones deleted before

    Examples:
      | count | secretName       |
      | 1     | listSecret       |
      | 2     | list-secret-name |
      | 3     | listSecret       |
      | 5     | list-secret-name |
      | 25    | listSecret       |
      | 42    | list-secret-name |
