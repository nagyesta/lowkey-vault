Feature: Key sign and verify

    @Key @KeyCreate @KeySign @RSA
    Scenario Outline: RSA_SIGN_01 An RSA key is created with the key client then used for sign and verify operations
        Given key API version <api> is used
        And a key client is created with the vault named keys-generic
        And an RSA key named <keyName> is prepared with <keySize> bits size without HSM
        And the key has sign,verify operations granted
        And the RSA key is created
        When the created key is used to sign <clearText> with <algorithm>
        And the signed value is not <clearText>
        And the signature of <clearText> is verified with <algorithm>
        Then the signature matches

        Examples:
            | api | keyName       | keySize | algorithm | clearText                                    |
            | 7.2 | signRsaKey-01 | 2048    | PS256     | The quick brown fox jumps over the lazy dog. |
            | 7.3 | signRsaKey-02 | 2048    | PS256     | The quick brown fox jumps over the lazy dog. |
            | 7.3 | signRsaKey-03 | 2048    | PS384     | <?xml version="1.0"?><none/>                 |
            | 7.3 | signRsaKey-04 | 2048    | PS512     | The quick brown fox jumps over the lazy dog. |
            | 7.3 | signRsaKey-05 | 4096    | RS256     | The quick brown fox jumps over the lazy dog. |
            | 7.3 | signRsaKey-06 | 4096    | RS384     | <?xml version="1.0"?><none/>                 |
            | 7.3 | signRsaKey-07 | 4096    | RS512     | The quick brown fox jumps over the lazy dog. |
            | 7.4 | signRsaKey-08 | 2048    | PS256     | The quick brown fox jumps over the lazy dog. |

    @Key @KeyCreate @KeySign @EC
    Scenario Outline: EC_SIGN_01 An EC key is created with the key client then used for sign and verify operations
        Given key API version <api> is used
        And a key client is created with the vault named keys-generic
        And an EC key named <keyName> is prepared with <curveName> and without HSM
        And the key has sign,verify operations granted
        And the EC key is created
        When the created key is used to sign <clearText> with <algorithm>
        And the signed value is not <clearText>
        And the signature of <clearText> is verified with <algorithm>
        Then the signature matches

        Examples:
            | api | keyName   | curveName | algorithm | clearText                                                        |
            | 7.2 | signEc-01 | P-256     | ES256     | The quick brown fox jumps over the lazy dog.                     |
            | 7.3 | signEc-02 | P-256     | ES256     | The quick brown fox jumps over the lazy dog.                     |
            | 7.3 | signEc-03 | P-256K    | ES256K    | The quick brown fox jumps over the lazy dog.                     |
            | 7.3 | signEc-04 | P-384     | ES384     | The quick brown fox jumps over the lazy dog.                     |
            | 7.3 | signEc-05 | P-521     | ES512     | The quick brown fox jumps over the lazy dog.                     |
            | 7.3 | signEc-06 | P-256     | ES256     | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |
            | 7.3 | signEc-07 | P-256K    | ES256K    | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |
            | 7.3 | signEc-08 | P-384     | ES384     | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |
            | 7.3 | signEc-09 | P-521     | ES512     | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |
            | 7.4 | signEc-10 | P-521     | ES512     | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |
