Feature: Key import

    @Key @KeyImport @KeySign @RSA
    Scenario Outline: RSA_IMPORT_01 An RSA key is imported with the key client then used for sign and verify operations
        Given key API version <api> is used
        And a key client is created with the vault named keys-generic
        And an RSA key is imported with <keyName> as name and <keySize> bits of key size without HSM
        When the created key is used to sign <clearText> with <algorithm>
        And the signed value is not <clearText>
        And the RSA signature of <clearText> is verified using the original public key with <algorithm>
        Then the signature matches

        Examples:
            | api | keyName         | keySize | algorithm | clearText                                    |
            | 7.2 | importRsaKey-01 | 2048    | PS256     | The quick brown fox jumps over the lazy dog. |
            | 7.3 | importRsaKey-02 | 2048    | PS256     | The quick brown fox jumps over the lazy dog. |
            | 7.3 | importRsaKey-03 | 2048    | PS384     | <?xml version="1.0"?><none/>                 |
            | 7.3 | importRsaKey-04 | 2048    | PS512     | The quick brown fox jumps over the lazy dog. |
            | 7.3 | importRsaKey-05 | 4096    | RS256     | The quick brown fox jumps over the lazy dog. |
            | 7.3 | importRsaKey-06 | 4096    | RS384     | <?xml version="1.0"?><none/>                 |
            | 7.3 | importRsaKey-07 | 4096    | RS512     | The quick brown fox jumps over the lazy dog. |
            | 7.4 | importRsaKey-08 | 2048    | PS256     | The quick brown fox jumps over the lazy dog. |
            | 7.5 | importRsaKey-09 | 2048    | PS256     | The quick brown fox jumps over the lazy dog. |
            | 7.6 | importRsaKey-10 | 2048    | PS256     | The quick brown fox jumps over the lazy dog. |

    @Key @KeyImport @KeySign @EC
    Scenario Outline: EC_IMPORT_01 An EC key is imported with the key client then used for sign and verify operations
        Given key API version <api> is used
        And a key client is created with the vault named keys-generic
        And an EC key is imported with <keyName> as name and <curveName> curve without HSM
        When the created key is used to sign <clearText> with <algorithm>
        And the signed value is not <clearText>
        And the EC signature of <clearText> is verified using the original public key with <algorithm>
        Then the signature matches

        Examples:
            | api | keyName     | curveName | algorithm | clearText                                                        |
            | 7.2 | importEc-01 | P-256     | ES256     | The quick brown fox jumps over the lazy dog.                     |
            | 7.3 | importEc-02 | P-256     | ES256     | The quick brown fox jumps over the lazy dog.                     |
            | 7.3 | importEc-03 | P-256K    | ES256K    | The quick brown fox jumps over the lazy dog.                     |
            | 7.3 | importEc-04 | P-384     | ES384     | The quick brown fox jumps over the lazy dog.                     |
            | 7.3 | importEc-05 | P-521     | ES512     | The quick brown fox jumps over the lazy dog.                     |
            | 7.3 | importEc-06 | P-256     | ES256     | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |
            | 7.3 | importEc-07 | P-256K    | ES256K    | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |
            | 7.3 | importEc-08 | P-384     | ES384     | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |
            | 7.3 | importEc-09 | P-521     | ES512     | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |
            | 7.4 | importEc-10 | P-256     | ES256     | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |
            | 7.5 | importEc-11 | P-256     | ES256     | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |
            | 7.6 | importEc-12 | P-256     | ES256     | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |


    @Key @KeyImport @KeyEncrypt @OCT
    Scenario Outline: OCT_IMPORT_01 An OCT key is imported with the key client then used for encrypt and decrypt operations
        Given key API version <api> is used
        And a key client is created with the vault named keys-generic
        And an OCT key is imported with <keyName> as name and <keySize> bits of key size with HSM
        When the created key is used to encrypt <clearText> with <algorithm>
        And the encrypted value is not <clearText>
        And the encrypted value is decrypted using the original OCT key using <algorithm>
        Then the decrypted value is <clearText>

        Examples:
            | api | keyName      | keySize | algorithm  | clearText                                                        |
            | 7.2 | importOct-01 | 128     | A128CBCPAD | The quick brown fox jumps over the lazy dog.                     |
            | 7.3 | importOct-02 | 128     | A128CBCPAD | The quick brown fox jumps over the lazy dog.                     |
            | 7.3 | importOct-03 | 192     | A192CBCPAD | The quick brown fox jumps over the lazy dog.                     |
            | 7.3 | importOct-04 | 256     | A256CBCPAD | The quick brown fox jumps over the lazy dog.                     |
            | 7.3 | importOct-05 | 128     | A128CBC    | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |
            | 7.3 | importOct-06 | 192     | A192CBC    | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |
            | 7.3 | importOct-07 | 256     | A256CBC    | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |
            | 7.4 | importOct-08 | 256     | A256CBC    | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |
            | 7.5 | importOct-09 | 256     | A256CBC    | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |
            | 7.6 | importOct-10 | 256     | A256CBC    | Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do. |
