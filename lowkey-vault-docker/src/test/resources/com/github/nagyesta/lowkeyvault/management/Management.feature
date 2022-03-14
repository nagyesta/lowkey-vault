Feature: Vault Management

  @CreateVault
  Scenario: MANAGEMENT_01 A vault is created, deleted, then recovered.
    Given a vault is created with name vault-management
    And vault list contains vault-management with deletedOn as null
    And deleted vault list does not contain vault-management
    When the vault named vault-management is deleted
    And deleted vault list contains vault-management with deletedOn populated
    And vault list does not contain vault-management
    And the vault named vault-management is recovered
    Then vault list contains vault-management with deletedOn as null
