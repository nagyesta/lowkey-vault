Feature: Secret get

    @Secret @SecretCreate @SecretGet
    Scenario Outline: SECRET_GET_01 Multiple versions of secrets are created with the secret client then the latest is fetched
        Given secret API version <api> is used
        And a secret client is created with the vault named secrets-generic
        And a secret named <secretName> and valued <secretValue> is prepared
        And <versionsCount> version of the secret is created
        And the secret is set to have <contentType> as content type
        And the secret is set to expire <expires> seconds after creation
        And the secret is set to be not usable until <notBefore> seconds after creation
        And the secret is set to use <tagMap> as tags
        And the secret is set to be enabled
        When the secret is created
        And the last secret version of <secretName> is fetched without providing a version
        Then the created secret exists with value: <secretValue>
        And the secret name is <secretName>
        And the secret URL contains the vault url and <secretName>
        And the secret enabled status is enabled
        And the secret expires <expires> seconds after creation
        And the secret is not usable before <notBefore> seconds after creation
        And the secret has <contentType> as content type
        And the secret has <tagMap> as tags
        And the secret recovery settings are default

        Examples:
            | api | versionsCount | secretName          | contentType      | secretValue                                  | expires | notBefore | tagMap            |
            | 7.2 | 2             | 72-get01Secret1     | text/plain       | abc123                                       | null    | null      | null              |
            | 7.3 | 2             | 73-get01Secret1     | text/plain       | abc123                                       | null    | null      | null              |
            | 7.3 | 1             | 73-get01Secret2     | text/plain       | The quick brown fox jumps over the lazy dog. | null    | null      | null              |
            | 7.3 | 2             | 73-get01-secret-1   | text/plain       | abc123                                       | null    | null      | null              |
            | 7.3 | 1             | 73-get01-secret-2   | text/plain       | The quick brown fox jumps over the lazy dog. | null    | null      | null              |
            | 7.3 | 4             | 73-get01Secret3     | text/plain       | Lorem ipsum                                  | null    | null      | null              |
            | 7.3 | 3             | 73-get01-secret-3   | text/plain       | Lorem ipsum                                  | null    | null      | null              |
            | 7.3 | 4             | 73-get01SecretMap1  | text/plain       | 123 XYZ                                      | null    | null      | aKey:aValue,b1:b2 |
            | 7.3 | 3             | 73-get01SecretMap2  | text/plain       | 123 XYZ                                      | null    | null      | aKey:aValue       |
            | 7.3 | 4             | 73-get01SecretXml   | application/xml  | <?xml version="1.0"?><none/>                 | null    | null      | null              |
            | 7.3 | 3             | 73-get01SecretJson  | application/json | {"value":true}                               | null    | null      | null              |
            | 7.3 | 4             | 73-get01SecretDates | text/plain       | Only sometimes.                              | 4321    | 1234      | null              |
            | 7.4 | 4             | 74-get01SecretDates | text/plain       | Only sometimes.                              | 4321    | 1234      | null              |
            | 7.5 | 4             | 75-get01SecretDates | text/plain       | Only sometimes.                              | 4321    | 1234      | null              |

    @Secret @SecretCreate @SecretGet
    Scenario Outline: SECRET_GET_02 Multiple versions of secrets are created with the secret client then the first is fetched
        Given secret API version <api> is used
        And a secret client is created with the vault named secrets-generic
        And a secret named <secretName> and valued <secretValue> is prepared
        And the secret is set to have <contentType> as content type
        And <versionsCount> version of the secret is created
        When the secret is created
        And the first secret version of <secretName> is fetched with providing a version
        Then the created secret exists with value: <secretValue>
        And the secret name is <secretName>
        And the secret URL contains the vault url and <secretName>
        And the secret has <contentType> as content type
        And the secret recovery settings are default

        Examples:
            | api | versionsCount | secretName        | contentType     | secretValue                                  |
            | 7.2 | 6             | 72-get02Secret1   | text/plain      | abc123                                       |
            | 7.3 | 6             | 73-get02Secret1   | text/plain      | abc123                                       |
            | 7.3 | 5             | 73-get02Secret2   | text/plain      | The quick brown fox jumps over the lazy dog. |
            | 7.3 | 4             | 73-get02SecretXml | application/xml | <?xml version="1.0"?><none/>                 |
            | 7.4 | 4             | 74-get02SecretXml | application/xml | <?xml version="1.0"?><none/>                 |
            | 7.5 | 4             | 75-get02SecretXml | application/xml | <?xml version="1.0"?><none/>                 |

    @Secret @SecretCreate @SecretGet
    Scenario Outline: SECRET_GET_03 Multiple versions of disabled secrets are created with the secret client then the latest is fetched
        Given secret API version <api> is used
        And a secret client is created with the vault named secrets-generic
        And a secret named <secretName> and valued <secretValue> is prepared
        And <versionsCount> version of the secret is created
        And the secret is set to have <contentType> as content type
        And the secret is set to be not enabled
        When the secret is created
        Then the last secret version of <secretName> cannot be fetched as it is not enabled

        Examples:
            | api | versionsCount | secretName               | contentType | secretValue |
            | 7.2 | 3             | 72-get03SecretNotEnabled | text/plain  | Not enabled |
            | 7.3 | 3             | 73-get03SecretNotEnabled | text/plain  | Not enabled |
            | 7.4 | 3             | 74-get03SecretNotEnabled | text/plain  | Not enabled |
            | 7.5 | 3             | 75-get03SecretNotEnabled | text/plain  | Not enabled |

    @Secret @SecretCreate @SecretUpdate @SecretGet
    Scenario Outline: SECRET_UPDATE_01 Multiple versions of secrets are created with the secret client then the latest is updated and fetched
        Given secret API version <api> is used
        And a secret client is created with the vault named secrets-generic
        And a secret named <secretName> and valued <secretValue> is prepared
        And the secret is set to have <contentType> as content type
        And <versionsCount> version of the secret is created
        When the last version of the secret is prepared for an update
        And the secret is updated to expire <expires> seconds after creation
        And the secret is updated to be not usable until <notBefore> seconds after creation
        And the secret is updated to use <tagMap> as tags
        And the secret is updated to be <enabledStatus>
        And the secret update request is sent
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
            | api | versionsCount | secretName                  | enabledStatus | contentType      | secretValue                                  | expires | notBefore | tagMap            |
            | 7.2 | 2             | 72-update01Secret1          | enabled       | text/plain       | abc123                                       | null    | null      | null              |
            | 7.3 | 2             | 73-update01Secret1          | enabled       | text/plain       | abc123                                       | null    | null      | null              |
            | 7.3 | 1             | 73-update01Secret2          | enabled       | text/plain       | The quick brown fox jumps over the lazy dog. | null    | null      | null              |
            | 7.3 | 2             | 73-update01-secret-1        | enabled       | text/plain       | abc123                                       | null    | null      | null              |
            | 7.3 | 1             | 73-update01-secret-2        | enabled       | text/plain       | The quick brown fox jumps over the lazy dog. | null    | null      | null              |
            | 7.3 | 4             | 73-update01Secret3          | enabled       | text/plain       | Lorem ipsum                                  | null    | null      | null              |
            | 7.3 | 3             | 73-update01-secret-3        | enabled       | text/plain       | Lorem ipsum                                  | null    | null      | null              |
            | 7.3 | 4             | 73-update01SecretMap1       | enabled       | text/plain       | 123 XYZ                                      | null    | null      | aKey:aValue,b1:b2 |
            | 7.3 | 3             | 73-update01SecretMap2       | enabled       | text/plain       | 123 XYZ                                      | null    | null      | aKey:aValue       |
            | 7.3 | 4             | 73-update01SecretXml        | enabled       | application/xml  | <?xml version="1.0"?><none/>                 | null    | null      | null              |
            | 7.3 | 3             | 73-update01SecretJson       | enabled       | application/json | {"value":true}                               | null    | null      | null              |
            | 7.3 | 4             | 73-update01SecretDates      | enabled       | text/plain       | Only sometimes.                              | 4321    | 1234      | null              |
            | 7.3 | 3             | 73-update01SecretNotEnabled | not enabled   | text/plain       | Not enabled                                  | null    | null      | null              |
            | 7.4 | 3             | 74-update01SecretJson       | enabled       | application/json | {"value":true}                               | null    | null      | null              |
            | 7.5 | 3             | 75-update01SecretJson       | enabled       | application/json | {"value":true}                               | null    | null      | null              |
