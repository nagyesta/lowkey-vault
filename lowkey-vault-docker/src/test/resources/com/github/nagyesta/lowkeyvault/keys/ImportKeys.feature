Feature: Key import

  @Key @KeyImport @KeySign @RSA
  Scenario Outline: RSA_IMPORT_01 An RSA key is imported with the key client then used for sign and verify operations
    Given a key client is created with the vault named keys-generic
    And an RSA key is imported with <keyName> as name and <keySize> bits of key size without HSM
    When the created key is used to sign <clearText> with <algorithm>
    And the signed value is not <clearText>
    And the RSA signature of <clearText> is verified using the original public key with <algorithm>
    Then the signature matches

    Examples:
      | keyName        | keySize | algorithm | clearText                                    |
      | importRsaKey-1 | 2048    | PS256     | The quick brown fox jumps over the lazy dog. |
      | importRsaKey-2 | 2048    | PS384     | <?xml version="1.0"?><none/>                 |
      | importRsaKey-3 | 2048    | PS512     | The quick brown fox jumps over the lazy dog. |
      | importRsaKey-4 | 4096    | RS256     | The quick brown fox jumps over the lazy dog. |
      | importRsaKey-5 | 4096    | RS384     | <?xml version="1.0"?><none/>                 |
      | importRsaKey-6 | 4096    | RS512     | The quick brown fox jumps over the lazy dog. |

  @Key @KeyImport @KeySign @EC
  Scenario Outline: EC_IMPORT_01 An EC key is imported with the key client then used for sign and verify operations
    Given a key client is created with the vault named keys-generic
    And an EC key is imported with <keyName> as name and <curveName> curve without HSM
    When the created key is used to sign <clearText> with <algorithm>
    And the signed value is not <clearText>
    And the EC signature of <clearText> is verified using the original public key with <algorithm>
    Then the signature matches

    Examples:
      | keyName    | curveName | algorithm | clearText                                                        |
      | importEc-1 | P-256     | ES256     | The quick brown fox jumps over the lazy dog.                     |
      | importEc-2 | P-256K    | ES256K    | The quick brown fox jumps over the lazy dog.                     |
      | importEc-3 | P-384     | ES384     | The quick brown fox jumps over the lazy dog.                     |
      | importEc-4 | P-521     | ES512     | The quick brown fox jumps over the lazy dog.                     |
      | importEc-5 | P-256     | ES256     | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |
      | importEc-6 | P-256K    | ES256K    | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |
      | importEc-7 | P-384     | ES384     | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |
      | importEc-8 | P-521     | ES512     | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |


  @Key @KeyImport @KeyEncrypt @OCT
  Scenario Outline: OCT_IMPORT_01 An OCT key is imported with the key client then used for encrypt and decrypt operations
    Given a key client is created with the vault named keys-generic
    And an OCT key is imported with <keyName> as name and <keySize> bits of key size with HSM
    When the created key is used to encrypt <clearText> with <algorithm>
    And the encrypted value is not <clearText>
    And the encrypted value is decrypted using the original OCT key using <algorithm>
    Then the decrypted value is <clearText>

    Examples:
      | keyName     | keySize | algorithm  | clearText                                                        |
      | importOct-1 | 128     | A128CBCPAD | The quick brown fox jumps over the lazy dog.                     |
      | importOct-2 | 192     | A192CBCPAD | The quick brown fox jumps over the lazy dog.                     |
      | importOct-3 | 256     | A256CBCPAD | The quick brown fox jumps over the lazy dog.                     |
      | importOct-4 | 128     | A128CBC    | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |
      | importOct-5 | 192     | A192CBC    | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |
      | importOct-6 | 256     | A256CBC    | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |
