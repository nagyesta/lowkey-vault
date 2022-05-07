Feature: Secret backup and restore

  @Secret @SecretCreate @DeleteVault @SecretBackup @SecretRestore @CreateVault
  Scenario Outline: SECRET_BACKUP_01 Secrets are created and backed up then vault is deleted and recreated to restore secret
    Given secret API version <api> is used
    And a vault is created with name secrets-backup-<index>
    And a secret client is created with the vault named secrets-backup-<index>
    And <count> secrets with <secretName>- prefix are created valued abc123
    And the secret named <secretName>-<count> is backed up
    And the vault named secrets-backup-<index> is deleted
    And the vault named secrets-backup-<index> is purged
    And a vault is created with name secrets-backup-<index>
    When the secret named <secretName>-<count> is restored
    Then the last secret version of <secretName>-<count> is fetched without providing a version
    And the created secret exists with value: abc123

    Examples:
      | api | index | count | secretName            |
      | 7.2 | 01    | 1     | 72-backupSecret       |
      | 7.2 | 02    | 2     | 72-backup-secret-name |
      | 7.2 | 03    | 3     | 72-backupSecret       |
      | 7.2 | 04    | 5     | 72-backup-secret-name |
      | 7.2 | 05    | 25    | 72-backupSecret       |
      | 7.2 | 06    | 42    | 72-backup-secret-name |
      | 7.3 | 07    | 1     | 73-backupSecret       |
      | 7.3 | 08    | 2     | 73-backup-secret-name |
      | 7.3 | 09    | 3     | 73-backupSecret       |
      | 7.3 | 10    | 5     | 73-backup-secret-name |
      | 7.3 | 11    | 25    | 73-backupSecret       |
      | 7.3 | 12    | 42    | 73-backup-secret-name |
