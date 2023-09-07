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
            | 7.3 | 02    | 1     | listSecret       |
            | 7.3 | 03    | 2     | list-secret-name |
            | 7.3 | 04    | 3     | listSecret       |
            | 7.3 | 05    | 5     | list-secret-name |
            | 7.3 | 06    | 25    | listSecret       |
            | 7.3 | 07    | 42    | list-secret-name |
            | 7.4 | 08    | 42    | list-secret-name |
