package com.github.nagyesta.lowkeyvault.keys;

import com.azure.core.util.Context;
import com.azure.security.keyvault.keys.KeyAsyncClient;
import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient;
import com.azure.security.keyvault.keys.cryptography.CryptographyClient;
import com.azure.security.keyvault.keys.cryptography.models.*;
import com.azure.security.keyvault.keys.models.*;
import com.github.nagyesta.abortmission.booster.jupiter.annotation.LaunchAbortArmed;
import com.github.nagyesta.lowkeyvault.http.ApacheHttpClientProvider;
import com.github.nagyesta.lowkeyvault.http.ClientProviderConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Stream;

@LaunchAbortArmed
@SpringBootTest(classes = ClientProviderConfig.class)
public class KeyEncryptDecryptAesTest extends BaseKeyTest {

    @Autowired
    public KeyEncryptDecryptAesTest(final ApacheHttpClientProvider provider) {
        super(provider);
    }

    public static Stream<Arguments> aesProvider() {
        final byte[] iv = "iv-parameter-val".getBytes(StandardCharsets.UTF_8);
        return Stream.<Arguments>builder()
                .add(Arguments.of("lorem ipsum dolo", KeyType.OCT_HSM, EncryptionAlgorithm.A128CBC, 128, iv))
                .add(Arguments.of("the quick brown fox jumped over ", KeyType.OCT_HSM, EncryptionAlgorithm.A128CBCPAD, 128, iv))
                .add(Arguments.of("lorem ipsum dolo", KeyType.OCT_HSM, EncryptionAlgorithm.A192CBC, 192, iv))
                .add(Arguments.of("the quick brown fox jumped over ", KeyType.OCT_HSM, EncryptionAlgorithm.A192CBCPAD, 192, iv))
                .add(Arguments.of("lorem ipsum dolo", KeyType.OCT_HSM, EncryptionAlgorithm.A256CBC, 256, iv))
                .add(Arguments.of("the quick brown fox jumped over ", KeyType.OCT_HSM, EncryptionAlgorithm.A256CBCPAD, 256, iv))
                .build();
    }

    @ParameterizedTest
    @MethodSource("aesProvider")
    @Tags({@Tag("create"), @Tag("rsa"), @Tag("encrypt"), @Tag("decrypt")})
    void testKeyShouldBeUsableForEncryptAndDecryptWhenPermissionsAreSetUpCorrectly(
            final String clear, final KeyType keyType, final EncryptionAlgorithm encryptionAlgorithm, final int size, final byte[] iv) {
        //given
        final KeyClient keyClient = provider.getKeyClient();
        final String name = randomName();
        final CreateKeyOptions options = new CreateOctKeyOptions(name).setKeySize(size)
                .setHardwareProtected(keyType == KeyType.OCT_HSM)
                .setKeyOperations(KeyOperation.ENCRYPT, KeyOperation.DECRYPT, KeyOperation.WRAP_KEY, KeyOperation.UNWRAP_KEY)
                .setEnabled(true);

        final KeyVaultKey key = keyClient.createKey(options);
        final CryptographyClient cryptoClient = provider.getCryptoClient(key.getKey().getId());

        //when
        final EncryptResult encryptResult = cryptoClient.encrypt(
                aesEncryptParams(encryptionAlgorithm, clear.getBytes(StandardCharsets.UTF_8), iv), Context.NONE);
        final DecryptResult decrypt = cryptoClient.decrypt(aesDecryptParams(encryptionAlgorithm, encryptResult), Context.NONE);

        //then
        Assertions.assertEquals(clear, new String(decrypt.getPlainText(), StandardCharsets.UTF_8));
    }

    @ParameterizedTest
    @MethodSource("aesProvider")
    @Tags({@Tag("create"), @Tag("aes"), @Tag("encrypt"), @Tag("decrypt")})
    void testKeyShouldBeUsableForEncryptAndDecryptWhenPermissionsAreSetUpCorrectlyAsync(
            final String clear, final KeyType keyType, final EncryptionAlgorithm encryptionAlgorithm, final int size, final byte[] iv) {
        //given
        final KeyAsyncClient keyClient = provider.getKeyAsyncClient();
        final String name = randomName();
        final CreateKeyOptions options = new CreateOctKeyOptions(name).setKeySize(size)
                .setHardwareProtected(keyType == KeyType.OCT_HSM)
                .setKeyOperations(KeyOperation.ENCRYPT, KeyOperation.DECRYPT, KeyOperation.WRAP_KEY, KeyOperation.UNWRAP_KEY)
                .setEnabled(true);

        final KeyVaultKey key = keyClient.createKey(options).block();
        final CryptographyAsyncClient cryptoClient = provider.getCryptoAsyncClient(Objects.requireNonNull(key).getKey().getId());

        //when
        final EncryptResult encryptResult = cryptoClient.encrypt(
                aesEncryptParams(encryptionAlgorithm, clear.getBytes(StandardCharsets.UTF_8), iv)).block();
        final DecryptResult decrypt = cryptoClient.decrypt(aesDecryptParams(encryptionAlgorithm, encryptResult)).block();

        //then
        Assertions.assertEquals(clear, new String(Objects.requireNonNull(decrypt).getPlainText(), StandardCharsets.UTF_8));
    }

    private EncryptParameters aesEncryptParams(final EncryptionAlgorithm encryptionAlgorithm, final byte[] clearText, final byte[] iv) {
        if (encryptionAlgorithm == EncryptionAlgorithm.A128CBC) {
            return EncryptParameters.createA128CbcParameters(clearText, iv);
        } else if (encryptionAlgorithm == EncryptionAlgorithm.A128CBCPAD) {
            return EncryptParameters.createA128CbcPadParameters(clearText, iv);
        } else if (encryptionAlgorithm == EncryptionAlgorithm.A192CBC) {
            return EncryptParameters.createA192CbcParameters(clearText, iv);
        } else if (encryptionAlgorithm == EncryptionAlgorithm.A192CBCPAD) {
            return EncryptParameters.createA192CbcPadParameters(clearText, iv);
        } else if (encryptionAlgorithm == EncryptionAlgorithm.A256CBC) {
            return EncryptParameters.createA256CbcParameters(clearText, iv);
        } else if (encryptionAlgorithm == EncryptionAlgorithm.A256CBCPAD) {
            return EncryptParameters.createA256CbcPadParameters(clearText, iv);
        } else {
            return null;
        }
    }

    private DecryptParameters aesDecryptParams(final EncryptionAlgorithm encryptionAlgorithm, final EncryptResult encryptResult) {
        if (encryptionAlgorithm == EncryptionAlgorithm.A128CBC) {
            return DecryptParameters.createA128CbcParameters(encryptResult.getCipherText(), encryptResult.getIv());
        } else if (encryptionAlgorithm == EncryptionAlgorithm.A128CBCPAD) {
            return DecryptParameters.createA128CbcPadParameters(encryptResult.getCipherText(), encryptResult.getIv());
        } else if (encryptionAlgorithm == EncryptionAlgorithm.A192CBC) {
            return DecryptParameters.createA192CbcParameters(encryptResult.getCipherText(), encryptResult.getIv());
        } else if (encryptionAlgorithm == EncryptionAlgorithm.A192CBCPAD) {
            return DecryptParameters.createA192CbcPadParameters(encryptResult.getCipherText(), encryptResult.getIv());
        } else if (encryptionAlgorithm == EncryptionAlgorithm.A256CBC) {
            return DecryptParameters.createA256CbcParameters(encryptResult.getCipherText(), encryptResult.getIv());
        } else if (encryptionAlgorithm == EncryptionAlgorithm.A256CBCPAD) {
            return DecryptParameters.createA256CbcPadParameters(encryptResult.getCipherText(), encryptResult.getIv());
        } else {
            return null;
        }
    }
}
