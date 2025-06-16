Feature: Secret list deleted

    @Secret @SecretCreate @SecretListDeleted @CreateVault
    Scenario Outline: SECRET_LIST_DELETED_01 Secrets are created and deleted with the secret client then all are listed as deleted secrets
        Given secret API version <api> is used
        And a vault is created with name secrets-del-<index>
        And a secret client is created with the vault named secrets-del-<index>
        And <count> secrets with <secretName>- prefix are created valued abc123
        And <count> secrets with <secretName>- prefix are deleted
        When the deleted secret properties are listed
        Then the listed deleted secrets are matching the ones deleted before

        Examples:
            | api | index | count | secretName       |
            | 7.2 | 01    | 1     | listSecret       |
            | 7.3 | 02    | 1     | listSecret       |
            | 7.3 | 03    | 2     | list-secret-name |
            | 7.3 | 04    | 3     | listSecret       |
            | 7.3 | 05    | 5     | list-secret-name |
            | 7.3 | 06    | 25    | listSecret       |
            | 7.3 | 07    | 42    | list-secret-name |
            | 7.4 | 08    | 5     | list-secret-name |
            | 7.5 | 09    | 5     | list-secret-name |
            | 7.6 | 10    | 5     | list-secret-name |
