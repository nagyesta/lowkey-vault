package com.github.nagyesta.lowkeyvault.keys;

import com.azure.core.util.Context;
import com.azure.security.keyvault.keys.KeyAsyncClient;
import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient;
import com.azure.security.keyvault.keys.cryptography.CryptographyClient;
import com.azure.security.keyvault.keys.cryptography.models.DecryptParameters;
import com.azure.security.keyvault.keys.cryptography.models.DecryptResult;
import com.azure.security.keyvault.keys.cryptography.models.EncryptResult;
import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
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
public class KeyEncryptDecryptRsaTest extends BaseKeyTest {

    @Autowired
    public KeyEncryptDecryptRsaTest(final ApacheHttpClientProvider provider) {
        super(provider);
    }

    public static Stream<Arguments> rsaProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of("lorem ipsum", KeyType.RSA, EncryptionAlgorithm.RSA_OAEP))
                .add(Arguments.of("the quick brown fox", KeyType.RSA, EncryptionAlgorithm.RSA_OAEP_256))
                .add(Arguments.of("jumped over", KeyType.RSA, EncryptionAlgorithm.RSA1_5))
                .add(Arguments.of("lorem ipsum", KeyType.RSA_HSM, EncryptionAlgorithm.RSA_OAEP))
                .add(Arguments.of("the quick brown fox", KeyType.RSA_HSM, EncryptionAlgorithm.RSA_OAEP_256))
                .add(Arguments.of("jumped over", KeyType.RSA_HSM, EncryptionAlgorithm.RSA1_5))
                .build();
    }

    @ParameterizedTest
    @MethodSource("rsaProvider")
    @Tags({@Tag("create"), @Tag("rsa"), @Tag("encrypt"), @Tag("decrypt")})
    void testKeyShouldBeUsableForEncryptAndDecryptWhenPermissionsAreSetUpCorrectly(
            final String clear, final KeyType keyType, final EncryptionAlgorithm encryptionAlgorithm) {
        //given
        final KeyClient keyClient = provider.getKeyClient();
        final String name = randomName();
        final CreateKeyOptions options = new CreateRsaKeyOptions(name).setKeySize(2048)
                .setHardwareProtected(keyType == KeyType.RSA_HSM)
                .setKeyOperations(KeyOperation.ENCRYPT, KeyOperation.DECRYPT, KeyOperation.WRAP_KEY, KeyOperation.UNWRAP_KEY)
                .setEnabled(true);

        final KeyVaultKey key = keyClient.createKey(options);
        final CryptographyClient cryptoClient = provider.getCryptoClient(key.getKey().getId());

        //when
        final EncryptResult encryptResult = cryptoClient.encrypt(encryptionAlgorithm, clear.getBytes(StandardCharsets.UTF_8));
        final DecryptResult decrypt = cryptoClient.decrypt(fromRsaParams(encryptionAlgorithm, encryptResult), Context.NONE);

        //then
        Assertions.assertEquals(clear, new String(decrypt.getPlainText(), StandardCharsets.UTF_8));
    }

    @ParameterizedTest
    @MethodSource("rsaProvider")
    @Tags({@Tag("create"), @Tag("rsa"), @Tag("encrypt"), @Tag("decrypt")})
    void testKeyShouldBeUsableForEncryptAndDecryptWhenPermissionsAreSetUpCorrectlyAsync(
            final String clear, final KeyType keyType, final EncryptionAlgorithm encryptionAlgorithm) {
        //given
        final KeyAsyncClient keyClient = provider.getKeyAsyncClient();
        final String name = randomName();
        final CreateKeyOptions options = new CreateRsaKeyOptions(name).setKeySize(2048)
                .setHardwareProtected(keyType == KeyType.RSA_HSM)
                .setKeyOperations(KeyOperation.ENCRYPT, KeyOperation.DECRYPT, KeyOperation.WRAP_KEY, KeyOperation.UNWRAP_KEY)
                .setEnabled(true);

        final KeyVaultKey key = keyClient.createKey(options).block();
        final CryptographyAsyncClient cryptoClient = provider.getCryptoAsyncClient(Objects.requireNonNull(key).getKey().getId());

        //when
        final EncryptResult encryptResult = cryptoClient.encrypt(encryptionAlgorithm, clear.getBytes(StandardCharsets.UTF_8)).block();
        final DecryptResult decrypt = cryptoClient.decrypt(fromRsaParams(encryptionAlgorithm, encryptResult)).block();

        //then
        Assertions.assertEquals(clear, new String(Objects.requireNonNull(decrypt).getPlainText(), StandardCharsets.UTF_8));
    }

    private DecryptParameters fromRsaParams(final EncryptionAlgorithm encryptionAlgorithm, final EncryptResult encryptResult) {
        if (encryptionAlgorithm == EncryptionAlgorithm.RSA1_5) {
            return DecryptParameters.createRsa15Parameters(encryptResult.getCipherText());
        } else if (encryptionAlgorithm == EncryptionAlgorithm.RSA_OAEP) {
            return DecryptParameters.createRsaOaepParameters(encryptResult.getCipherText());
        } else {
            return DecryptParameters.createRsaOaep256Parameters(encryptResult.getCipherText());
        }
    }
}
