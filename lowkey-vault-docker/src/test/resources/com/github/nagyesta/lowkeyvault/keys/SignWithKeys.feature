Feature: Key sign and verify

  @Key @KeyCreate @KeySign @RSA
  Scenario Outline: RSA_SIGN_01 An RSA key is created with the key client then used for sign and verify operations
    Given a key client is created with the vault named keys-generic
    And an RSA key named <keyName> is prepared with <keySize> bits size without HSM
    And the key has sign,verify operations granted
    And the RSA key is created
    When the created key is used to sign <clearText> with <algorithm>
    And the signed value is not <clearText>
    And the signature of <clearText> is verified with <algorithm>
    Then the signature matches

    Examples:
      | keyName      | keySize | algorithm | clearText                                    |
      | signRsaKey-1 | 2048    | PS256     | The quick brown fox jumps over the lazy dog. |
      | signRsaKey-2 | 2048    | PS384     | <?xml version="1.0"?><none/>                 |
      | signRsaKey-3 | 2048    | PS512     | The quick brown fox jumps over the lazy dog. |
      | signRsaKey-4 | 4096    | RS256     | The quick brown fox jumps over the lazy dog. |
      | signRsaKey-5 | 4096    | RS384     | <?xml version="1.0"?><none/>                 |
      | signRsaKey-6 | 4096    | RS512     | The quick brown fox jumps over the lazy dog. |

  @Key @KeyCreate @KeySign @EC
  Scenario Outline: EC_SIGN_01 An EC key is created with the key client then used for sign and verify operations
    Given a key client is created with the vault named keys-generic
    And an EC key named <keyName> is prepared with <curveName> and without HSM
    And the key has sign,verify operations granted
    And the EC key is created
    When the created key is used to sign <clearText> with <algorithm>
    And the signed value is not <clearText>
    And the signature of <clearText> is verified with <algorithm>
    Then the signature matches

    Examples:
      | keyName  | curveName | algorithm | clearText                                                        |
      | signEc-1 | P-256     | ES256     | The quick brown fox jumps over the lazy dog.                     |
      | signEc-2 | P-256K    | ES256K    | The quick brown fox jumps over the lazy dog.                     |
      | signEc-3 | P-384     | ES384     | The quick brown fox jumps over the lazy dog.                     |
      | signEc-4 | P-521     | ES512     | The quick brown fox jumps over the lazy dog.                     |
      | signEc-5 | P-256     | ES256     | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |
      | signEc-6 | P-256K    | ES256K    | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |
      | signEc-7 | P-384     | ES384     | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |
      | signEc-8 | P-521     | ES512     | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |
