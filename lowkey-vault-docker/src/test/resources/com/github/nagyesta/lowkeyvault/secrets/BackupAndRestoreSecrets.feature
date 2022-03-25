Feature: Secret backup and restore

  @Secret @SecretCreate @DeleteVault @SecretBackup @SecretRestore @CreateVault
  Scenario Outline: SECRET_BACKUP_01 Secrets are created and backed up then vault is deleted and recreated to restore secret
    Given a vault is created with name secrets-backup-<count>
    And a secret client is created with the vault named secrets-backup-<count>
    And <count> secrets with <secretName>- prefix are created valued abc123
    And the secret named <secretName>-<count> is backed up
    And the vault named secrets-backup-<count> is deleted
    And the vault named secrets-backup-<count> is purged
    And a vault is created with name secrets-backup-<count>
    When the secret named <secretName>-<count> is restored
    Then the last secret version of <secretName>-<count> is fetched without providing a version
    And the created secret exists with value: abc123

    Examples:
      | count | secretName         |
      | 1     | backupSecret       |
      | 2     | backup-secret-name |
      | 3     | backupSecret       |
      | 5     | backup-secret-name |
      | 25    | backupSecret       |
      | 42    | backup-secret-name |
