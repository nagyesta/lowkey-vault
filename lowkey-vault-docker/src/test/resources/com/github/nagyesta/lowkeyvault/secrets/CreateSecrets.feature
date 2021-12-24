Feature: Secret creation

  @Secret @SecretCreate
  Scenario Outline: SECRET_CREATE_01 Single versions of secrets can be created with the secrets client
    Given a secret client is created with the vault named secrets-generic
    And a secret named <secretName> and valued <secretValue> is prepared
    And the secret is set to have <contentType> as content type
    And the secret is set to expire <expires> seconds after creation
    And the secret is set to be not usable until <notBefore> seconds after creation
    And the secret is set to use <tagMap> as tags
    And the secret is set to be <enabledStatus>
    When the secret is created
    Then the created secret exists with value: <secretValue>
    And the secret name is <secretName>
    And the secret URL contains the vault url and <secretName>
    And the secret enabled status is <enabledStatus>
    And the secret expires <expires> seconds after creation
    And the secret is not usable before <notBefore> seconds after creation
    And the secret has <contentType> as content type
    And the secret has <tagMap> as tags
    And the secret recovery settings are default

    Examples:
      | secretName             | enabledStatus | contentType      | secretValue                                  | expires | notBefore | tagMap            |
      | createSecret1          | enabled       | text/plain       | abc123                                       | null    | null      | null              |
      | createSecret2          | enabled       | text/plain       | The quick brown fox jumps over the lazy dog. | null    | null      | null              |
      | create-secret-1        | enabled       | text/plain       | abc123                                       | null    | null      | null              |
      | create-secret-2        | enabled       | text/plain       | The quick brown fox jumps over the lazy dog. | null    | null      | null              |
      | createSecret3          | enabled       | text/plain       | Lorem ipsum                                  | null    | null      | null              |
      | create-secret-3        | enabled       | text/plain       | Lorem ipsum                                  | null    | null      | null              |
      | createSecretMap1       | enabled       | text/plain       | 123 XYZ                                      | null    | null      | aKey:aValue,b1:b2 |
      | createSecretMap2       | enabled       | text/plain       | 123 XYZ                                      | null    | null      | aKey:aValue       |
      | createSecretXml        | enabled       | application/xml  | <?xml version="1.0"?><none/>                 | null    | null      | null              |
      | createSecretJson       | enabled       | application/json | {"value":true}                               | null    | null      | null              |
      | createSecretDates      | enabled       | text/plain       | Only sometimes.                              | 4321    | 1234      | null              |
      | createSecretNotEnabled | not enabled   | text/plain       | Not enabled                                  | null    | null      | null              |
