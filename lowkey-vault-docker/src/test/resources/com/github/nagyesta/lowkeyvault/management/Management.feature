@Sequential
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

  @CreateVault @TimeShift
  Scenario: MANAGEMENT_02 A vault is created, deleted then time is shifted by 91 days to let it expire.
    Given a vault is created with name vault-management-time
    And the vault named vault-management-time is deleted
    And deleted vault list contains vault-management-time with deletedOn populated
    When the time of the vault named vault-management-time is shifted by 91 days
    Then deleted vault list does not contain vault-management-time

  @CreateVault @TimeShift
  Scenario: MANAGEMENT_03 A vault is created, time is shifted by 42 days for all vaults then change is verified.
    Given a vault is created with name vault-management-time-all
    And vault list is saved as original
    When the time of all vaults is shifted by 42 days
    # Then
    And vault list is saved as updated
    And the time stamps of original and updated differ by 42 days
