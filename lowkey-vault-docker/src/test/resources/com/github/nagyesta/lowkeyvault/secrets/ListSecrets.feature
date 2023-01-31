Feature: Secret list

    @Secret @SecretCreate @SecretList @CreateVault
    Scenario Outline: SECRET_LIST_01 Secrets are created with the secret client then all are listed
        Given secret API version <api> is used
        And a vault is created with name secrets-list-<index>
        And a secret client is created with the vault named secrets-list-<index>
        And <count> secrets with <secretName>- prefix are created valued abc123
        When the secret properties are listed
        Then the listed secrets are matching the ones created

        Examples:
            | api | index | count | secretName       |
            | 7.2 | 01    | 1     | listSecret       |
            | 7.2 | 02    | 2     | list-secret-name |
            | 7.2 | 03    | 3     | listSecret       |
            | 7.2 | 04    | 5     | list-secret-name |
            | 7.2 | 05    | 25    | listSecret       |
            | 7.2 | 06    | 42    | list-secret-name |
            | 7.3 | 07    | 1     | listSecret       |
            | 7.3 | 08    | 2     | list-secret-name |
            | 7.3 | 09    | 3     | listSecret       |
            | 7.3 | 10    | 5     | list-secret-name |
            | 7.3 | 11    | 25    | listSecret       |
            | 7.3 | 12    | 42    | list-secret-name |
