Feature: Secret delete and recover

    @Secret @SecretCreate @SecretDelete @SecretAlias
    Scenario Outline: SECRET_DELETE_01 Multiple versions of secrets are created with the secret client then deleted
        Given secret API version <api> is used
        And a secret client is created with the vault named <vaultName>
        And a secret named <secretName> and valued <secretValue> is prepared
        And the secret is set to have <contentType> as content type
        And <versionsCount> version of the secret is created
        When the secret is deleted
        Then the deleted secret recovery id contains the vault url and <secretName>
        And the secret recovery timestamps are default

        Examples:
            | api | vaultName            | versionsCount | secretName          | contentType     | secretValue                                  |
            | 7.2 | secrets-delete       | 6             | 72-deleteSecret1    | text/plain      | abc123                                       |
            | 7.3 | secrets-delete       | 6             | 73-deleteSecret1    | text/plain      | abc123                                       |
            | 7.3 | secrets-delete       | 5             | 73-deleteSecret2    | text/plain      | The quick brown fox jumps over the lazy dog. |
            | 7.3 | secrets-delete       | 4             | 73-deleteSecretXml  | application/xml | <?xml version="1.0"?><none/>                 |
            | 7.3 | secrets-alias-delete | 6             | 73-deleteSecret1A   | text/plain      | abc123                                       |
            | 7.3 | secrets-alias-delete | 5             | 73-deleteSecret2A   | text/plain      | The quick brown fox jumps over the lazy dog. |
            | 7.3 | secrets-alias-delete | 4             | 73-deleteSecretXmlA | application/xml | <?xml version="1.0"?><none/>                 |
            | 7.4 | secrets-alias-delete | 4             | 74-deleteSecretXmlA | application/xml | <?xml version="1.0"?><none/>                 |

    @Secret @SecretCreate @SecretDelete @SecretRecover @SecretAlias
    Scenario Outline: SECRET_RECOVER_01 Multiple versions of secrets are created with the secret client then deleted and recovered
        Given secret API version <api> is used
        And a secret client is created with the vault named <vaultName>
        And a secret named <secretName> and valued <secretValue> is prepared
        And the secret is set to have <contentType> as content type
        And <versionsCount> version of the secret is created
        And the secret is deleted
        When secret is recovered
        Then the secret URL contains the vault url and <secretName>

        Examples:
            | api | vaultName            | versionsCount | secretName           | contentType     | secretValue                                  |
            | 7.2 | secrets-delete       | 6             | 72-recoverSecret1    | text/plain      | abc123                                       |
            | 7.3 | secrets-delete       | 6             | 73-recoverSecret1    | text/plain      | abc123                                       |
            | 7.3 | secrets-delete       | 5             | 73-recoverSecret2    | text/plain      | The quick brown fox jumps over the lazy dog. |
            | 7.3 | secrets-delete       | 4             | 73-recoverSecretXml  | application/xml | <?xml version="1.0"?><none/>                 |
            | 7.3 | secrets-alias-delete | 6             | 73-recoverSecret1A   | text/plain      | abc123                                       |
            | 7.3 | secrets-alias-delete | 5             | 73-recoverSecret2A   | text/plain      | The quick brown fox jumps over the lazy dog. |
            | 7.3 | secrets-alias-delete | 4             | 73-recoverSecretXmlA | application/xml | <?xml version="1.0"?><none/>                 |
            | 7.4 | secrets-delete       | 5             | 74-recoverSecret2    | text/plain      | The quick brown fox jumps over the lazy dog. |

    @Secret @SecretCreate @SecretDelete @SecretPurge
    Scenario Outline: SECRET_PURGE_01 Multiple versions of secrets are created with the secret client then deleted and purged
        Given secret API version <api> is used
        And a vault is created with name secrets-purge-<secretName>
        And a secret client is created with the vault named secrets-purge-<secretName>
        And a secret named <secretName> and valued <secretValue> is prepared
        And the secret is set to have <contentType> as content type
        And <versionsCount> version of the secret is created
        And the secret is deleted
        When the secret is purged
        Then the deleted secret properties are listed
        And the listed deleted secrets are empty
        And the vault named secrets-purge-<secretName> is deleted
        And the vault named secrets-purge-<secretName> is purged

        Examples:
            | api | versionsCount | secretName        | contentType     | secretValue                                  |
            | 7.2 | 6             | 72-purgeSecret1   | text/plain      | abc123                                       |
            | 7.3 | 6             | 73-purgeSecret1   | text/plain      | abc123                                       |
            | 7.3 | 5             | 73-purgeSecret2   | text/plain      | The quick brown fox jumps over the lazy dog. |
            | 7.3 | 4             | 73-purgeSecretXml | application/xml | <?xml version="1.0"?><none/>                 |
            | 7.4 | 4             | 74-purgeSecretXml | application/xml | <?xml version="1.0"?><none/>                 |
