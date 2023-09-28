@Sequential
Feature: Vault Management

    Scenario: SWAGGER_01 Swagger UI is available
        When OpenAPI ui is available

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

    @CreateVault @TimeShift @Key @KeyCreate @EC
    Scenario: MANAGEMENT_04 A vault is created with a key rotation policy then time is shifted by 91 days to trigger auto rotate.
        Given a vault is created with name vault-management-time-auto-rotate
        And key API version 7.3 is used
        And a key client is created with the vault named vault-management-time-auto-rotate
        And an EC key named auto-rotate is prepared with P-256K and with HSM
        And 1 version of the EC key is created
        And the rotation policy is set to rotate after 30 days with expiry of 37 days
        When the time of the vault named vault-management-time-auto-rotate is shifted by 91 days
        Then the key named auto-rotate has 4 versions
        And the rotation policy of auto-rotate is rotating after 30 days with expiry of 37 days
