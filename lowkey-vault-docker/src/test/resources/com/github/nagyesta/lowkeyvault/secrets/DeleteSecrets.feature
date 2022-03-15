Feature: Secret delete and recover

  @Secret @SecretCreate @SecretDelete
  Scenario Outline: SECRET_DELETE_01 Multiple versions of secrets are created with the secret client then deleted
    Given a secret client is created with the vault named secrets-delete
    And a secret named <secretName> and valued <secretValue> is prepared
    And the secret is set to have <contentType> as content type
    And <versionsCount> version of the secret is created
    When the secret is deleted
    Then the deleted secret recovery id contains the vault url and <secretName>
    And the secret recovery timestamps are default

    Examples:
      | versionsCount | secretName      | contentType     | secretValue                                  |
      | 6             | deleteSecret1   | text/plain      | abc123                                       |
      | 5             | deleteSecret2   | text/plain      | The quick brown fox jumps over the lazy dog. |
      | 4             | deleteSecretXml | application/xml | <?xml version="1.0"?><none/>                 |

  @Secret @SecretCreate @SecretDelete @SecretRecover
  Scenario Outline: SECRET_RECOVER_01 Multiple versions of secrets are created with the secret client then deleted and recovered
    Given a secret client is created with the vault named secrets-delete
    And a secret named <secretName> and valued <secretValue> is prepared
    And the secret is set to have <contentType> as content type
    And <versionsCount> version of the secret is created
    And the secret is deleted
    When secret is recovered
    Then the secret URL contains the vault url and <secretName>

    Examples:
      | versionsCount | secretName       | contentType     | secretValue                                  |
      | 6             | recoverSecret1   | text/plain      | abc123                                       |
      | 5             | recoverSecret2   | text/plain      | The quick brown fox jumps over the lazy dog. |
      | 4             | recoverSecretXml | application/xml | <?xml version="1.0"?><none/>                 |

  @Secret @SecretCreate @SecretDelete @SecretPurge
  Scenario Outline: SECRET_PURGE_01 Multiple versions of secrets are created with the secret client then deleted and purged
    Given a vault is created with name secrets-purge-<secretName>
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
      | versionsCount | secretName     | contentType     | secretValue                                  |
      | 6             | purgeSecret1   | text/plain      | abc123                                       |
      | 5             | purgeSecret2   | text/plain      | The quick brown fox jumps over the lazy dog. |
      | 4             | purgeSecretXml | application/xml | <?xml version="1.0"?><none/>                 |
