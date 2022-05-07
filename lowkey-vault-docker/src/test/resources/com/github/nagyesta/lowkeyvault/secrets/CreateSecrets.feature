Feature: Secret creation

  @Secret @SecretCreate
  Scenario Outline: SECRET_CREATE_01 Single versions of secrets can be created with the secrets client
    Given secret API version <api> is used
    And a secret client is created with the vault named secrets-generic
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
      | api | secretName                | enabledStatus | contentType      | secretValue                                  | expires | notBefore | tagMap            |
      | 7.2 | 72-createSecret1          | enabled       | text/plain       | abc123                                       | null    | null      | null              |
      | 7.2 | 72-createSecret2          | enabled       | text/plain       | The quick brown fox jumps over the lazy dog. | null    | null      | null              |
      | 7.2 | 72-create-secret-1        | enabled       | text/plain       | abc123                                       | null    | null      | null              |
      | 7.2 | 72-create-secret-2        | enabled       | text/plain       | The quick brown fox jumps over the lazy dog. | null    | null      | null              |
      | 7.2 | 72-createSecret3          | enabled       | text/plain       | Lorem ipsum                                  | null    | null      | null              |
      | 7.2 | 72-create-secret-3        | enabled       | text/plain       | Lorem ipsum                                  | null    | null      | null              |
      | 7.2 | 72-createSecretMap1       | enabled       | text/plain       | 123 XYZ                                      | null    | null      | aKey:aValue,b1:b2 |
      | 7.2 | 72-createSecretMap2       | enabled       | text/plain       | 123 XYZ                                      | null    | null      | aKey:aValue       |
      | 7.2 | 72-createSecretXml        | enabled       | application/xml  | <?xml version="1.0"?><none/>                 | null    | null      | null              |
      | 7.2 | 72-createSecretJson       | enabled       | application/json | {"value":true}                               | null    | null      | null              |
      | 7.2 | 72-createSecretDates      | enabled       | text/plain       | Only sometimes.                              | 4321    | 1234      | null              |
      | 7.2 | 72-createSecretNotEnabled | not enabled   | text/plain       | Not enabled                                  | null    | null      | null              |
      | 7.3 | 73-createSecret1          | enabled       | text/plain       | abc123                                       | null    | null      | null              |
      | 7.3 | 73-createSecret2          | enabled       | text/plain       | The quick brown fox jumps over the lazy dog. | null    | null      | null              |
      | 7.3 | 73-create-secret-1        | enabled       | text/plain       | abc123                                       | null    | null      | null              |
      | 7.3 | 73-create-secret-2        | enabled       | text/plain       | The quick brown fox jumps over the lazy dog. | null    | null      | null              |
      | 7.3 | 73-createSecret3          | enabled       | text/plain       | Lorem ipsum                                  | null    | null      | null              |
      | 7.3 | 73-create-secret-3        | enabled       | text/plain       | Lorem ipsum                                  | null    | null      | null              |
      | 7.3 | 73-createSecretMap1       | enabled       | text/plain       | 123 XYZ                                      | null    | null      | aKey:aValue,b1:b2 |
      | 7.3 | 73-createSecretMap2       | enabled       | text/plain       | 123 XYZ                                      | null    | null      | aKey:aValue       |
      | 7.3 | 73-createSecretXml        | enabled       | application/xml  | <?xml version="1.0"?><none/>                 | null    | null      | null              |
      | 7.3 | 73-createSecretJson       | enabled       | application/json | {"value":true}                               | null    | null      | null              |
      | 7.3 | 73-createSecretDates      | enabled       | text/plain       | Only sometimes.                              | 4321    | 1234      | null              |
      | 7.3 | 73-createSecretNotEnabled | not enabled   | text/plain       | Not enabled                                  | null    | null      | null              |
