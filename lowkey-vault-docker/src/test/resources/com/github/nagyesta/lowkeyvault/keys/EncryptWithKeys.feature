Feature: Key encrypt and decrypt

    @Key @KeyCreate @KeyEncrypt @RSA
    Scenario Outline: RSA_ENCRYPT_01 An RSA key is created with the key client then used for encrypt and decrypt operations
        Given key API version <api> is used
        And a key client is created with the vault named keys-generic
        And an RSA key named <keyName> is prepared with <keySize> bits size without HSM
        And the key has encrypt,decrypt,wrapKey,unwrapKey operations granted
        And the RSA key is created
        When the created key is used to encrypt <clearText> with <algorithm>
        And the encrypted value is not <clearText>
        And the encrypted value is decrypted with <algorithm>
        Then the decrypted value is <clearText>

        Examples:
            | api | keyName          | keySize | algorithm    | clearText                                    |
            | 7.2 | encryptRsaKey-01 | 2048    | RSA1_5       | The quick brown fox jumps over the lazy dog. |
            | 7.2 | encryptRsaKey-02 | 2048    | RSA1_5       | <?xml version="1.0"?><none/>                 |
            | 7.2 | encryptRsaKey-03 | 2048    | RSA-OAEP     | The quick brown fox jumps over the lazy dog. |
            | 7.2 | encryptRsaKey-04 | 4096    | RSA-OAEP     | The quick brown fox jumps over the lazy dog. |
            | 7.2 | encryptRsaKey-05 | 2048    | RSA-OAEP-256 | <?xml version="1.0"?><none/>                 |
            | 7.2 | encryptRsaKey-06 | 4096    | RSA-OAEP-256 | <?xml version="1.0"?><none/>                 |
            | 7.3 | encryptRsaKey-07 | 2048    | RSA1_5       | The quick brown fox jumps over the lazy dog. |
            | 7.3 | encryptRsaKey-08 | 2048    | RSA1_5       | <?xml version="1.0"?><none/>                 |
            | 7.3 | encryptRsaKey-09 | 2048    | RSA-OAEP     | The quick brown fox jumps over the lazy dog. |
            | 7.3 | encryptRsaKey-10 | 4096    | RSA-OAEP     | The quick brown fox jumps over the lazy dog. |
            | 7.3 | encryptRsaKey-11 | 2048    | RSA-OAEP-256 | <?xml version="1.0"?><none/>                 |
            | 7.3 | encryptRsaKey-12 | 4096    | RSA-OAEP-256 | <?xml version="1.0"?><none/>                 |

    @Key @KeyCreate @KeyEncrypt @OCT
    Scenario Outline: OCT_ENCRYPT_01 An OCT key is created with the key client then used for encrypt and decrypt operations
        Given key API version <api> is used
        And a key client is created with the vault named keys-generic
        And an OCT key named <keyName> is prepared with <keySize> bits size
        And the key has encrypt,decrypt,wrapKey,unwrapKey operations granted
        And the OCT key is created
        When the created key is used to encrypt <clearText> with <algorithm>
        And the encrypted value is not <clearText>
        And the encrypted value is decrypted with <algorithm>
        Then the decrypted value is <clearText>

        Examples:
            | api | keyName       | keySize | algorithm  | clearText                                                        |
            | 7.2 | encryptOct-01 | 128     | A128CBCPAD | The quick brown fox jumps over the lazy dog.                     |
            | 7.2 | encryptOct-02 | 192     | A192CBCPAD | The quick brown fox jumps over the lazy dog.                     |
            | 7.2 | encryptOct-03 | 256     | A256CBCPAD | The quick brown fox jumps over the lazy dog.                     |
            | 7.2 | encryptOct-04 | 128     | A128CBC    | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |
            | 7.2 | encryptOct-05 | 192     | A192CBC    | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |
            | 7.2 | encryptOct-06 | 256     | A256CBC    | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |
            | 7.3 | encryptOct-07 | 128     | A128CBCPAD | The quick brown fox jumps over the lazy dog.                     |
            | 7.3 | encryptOct-08 | 192     | A192CBCPAD | The quick brown fox jumps over the lazy dog.                     |
            | 7.3 | encryptOct-09 | 256     | A256CBCPAD | The quick brown fox jumps over the lazy dog.                     |
            | 7.3 | encryptOct-10 | 128     | A128CBC    | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |
            | 7.3 | encryptOct-11 | 192     | A192CBC    | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |
            | 7.3 | encryptOct-12 | 256     | A256CBC    | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |
