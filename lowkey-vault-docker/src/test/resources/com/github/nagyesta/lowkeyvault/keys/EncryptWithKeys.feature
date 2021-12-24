Feature: Key encrypt and decrypt

  @Key @KeyCreate @KeyEncrypt @RSA
  Scenario Outline: RSA_ENCRYPT_01 An RSA key is created with the key client then used for encrypt and decrypt operations
    Given a key client is created with the vault named keys-generic
    And an RSA key named <keyName> is prepared with <keySize> bits size without HSM
    And the key has encrypt,decrypt,wrapKey,unwrapKey operations granted
    And the RSA key is created
    When the created key is used to encrypt <clearText> with <algorithm>
    And the encrypted value is not <clearText>
    And the encrypted value is decrypted with <algorithm>
    Then the decrypted value is <clearText>

    Examples:
      | keyName         | keySize | algorithm    | clearText                                    |
      | encryptRsaKey-1 | 2048    | RSA1_5       | The quick brown fox jumps over the lazy dog. |
      | encryptRsaKey-2 | 2048    | RSA1_5       | <?xml version="1.0"?><none/>                 |
      | encryptRsaKey-3 | 2048    | RSA-OAEP     | The quick brown fox jumps over the lazy dog. |
      | encryptRsaKey-4 | 4096    | RSA-OAEP     | The quick brown fox jumps over the lazy dog. |
      | encryptRsaKey-5 | 2048    | RSA-OAEP-256 | <?xml version="1.0"?><none/>                 |
      | encryptRsaKey-6 | 4096    | RSA-OAEP-256 | <?xml version="1.0"?><none/>                 |

  @Key @KeyCreate @KeyEncrypt @OCT
  Scenario Outline: OCT_ENCRYPT_01 An OCT key is created with the key client then used for encrypt and decrypt operations
    Given a key client is created with the vault named keys-generic
    And an OCT key named <keyName> is prepared with <keySize> bits size
    And the key has encrypt,decrypt,wrapKey,unwrapKey operations granted
    And the OCT key is created
    When the created key is used to encrypt <clearText> with <algorithm>
    And the encrypted value is not <clearText>
    And the encrypted value is decrypted with <algorithm>
    Then the decrypted value is <clearText>

    Examples:
      | keyName      | keySize | algorithm  | clearText                                                        |
      | encryptOct-1 | 128     | A128CBCPAD | The quick brown fox jumps over the lazy dog.                     |
      | encryptOct-2 | 192     | A192CBCPAD | The quick brown fox jumps over the lazy dog.                     |
      | encryptOct-3 | 256     | A256CBCPAD | The quick brown fox jumps over the lazy dog.                     |
      | encryptOct-4 | 128     | A128CBC    | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |
      | encryptOct-5 | 192     | A192CBC    | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |
      | encryptOct-6 | 256     | A256CBC    | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |
