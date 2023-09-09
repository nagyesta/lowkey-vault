Feature: Random data

    @RandomData
    Scenario Outline: RANDOM_DATA_01 The vault is used for getting random bytes
        Given key API version <api> is used
        And a key client is created with the vault named default
        When the vault is called for <count> bytes of random data
        Then the length of the random data is <count> bytes

        Examples:
            | api | count |
            | 7.3 | 1     |
            | 7.3 | 2     |
            | 7.3 | 3     |
            | 7.3 | 5     |
            | 7.3 | 10    |
            | 7.3 | 32    |
            | 7.3 | 63    |
            | 7.3 | 64    |
            | 7.3 | 128   |
            | 7.3 | 1024  |
            | 7.3 | 2048  |
            | 7.3 | 15000 |
            | 7.3 | 24000 |
            | 7.4 | 1     |
            | 7.4 | 42    |
