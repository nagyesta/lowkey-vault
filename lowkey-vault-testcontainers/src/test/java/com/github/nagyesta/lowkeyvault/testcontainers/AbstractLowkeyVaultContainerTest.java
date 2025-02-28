package com.github.nagyesta.lowkeyvault.testcontainers;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.policy.FixedDelay;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.Context;
import com.azure.security.keyvault.keys.KeyClientBuilder;
import com.azure.security.keyvault.keys.KeyServiceVersion;
import com.azure.security.keyvault.keys.cryptography.CryptographyClientBuilder;
import com.azure.security.keyvault.keys.cryptography.CryptographyServiceVersion;
import com.azure.security.keyvault.keys.cryptography.models.DecryptParameters;
import com.azure.security.keyvault.keys.cryptography.models.EncryptParameters;
import com.azure.security.keyvault.keys.models.CreateOctKeyOptions;
import com.azure.security.keyvault.keys.models.KeyOperation;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.SecretServiceVersion;
import org.junit.jupiter.api.Assertions;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class AbstractLowkeyVaultContainerTest {

    private static final String NAME = "name";
    private static final String VALUE = "value";
    private static final int SUCCESS = 200;

    protected String getCurrentLowkeyVaultImageName() {
        return "lowkey-vault:" + System.getProperty("imageVersion");
    }

    protected void verifyConnectionIsWorking(final String vaultUrl, final HttpClient httpClient, final TokenCredential credential) {
        Assertions.assertFalse(vaultUrl.endsWith(":8443"), "Vault URL should use random port instead of '8443'");

        final var secretClient = new SecretClientBuilder()
                .vaultUrl(vaultUrl)
                .credential(credential)
                .httpClient(httpClient)
                .serviceVersion(SecretServiceVersion.V7_3)
                .disableChallengeResourceVerification()
                .retryPolicy(new RetryPolicy(new FixedDelay(0, Duration.ZERO)))
                .buildClient();
        final var keyClient = new KeyClientBuilder()
                .vaultUrl(vaultUrl)
                .credential(credential)
                .httpClient(httpClient)
                .serviceVersion(KeyServiceVersion.V7_3)
                .disableChallengeResourceVerification()
                .retryPolicy(new RetryPolicy(new FixedDelay(0, Duration.ZERO)))
                .buildClient();

        final var secret = secretClient.setSecret(NAME, VALUE);
        Assertions.assertNotNull(secret);
        Assertions.assertTrue(secret.getId().startsWith(vaultUrl), "Secret Id should start with vault URL '" + vaultUrl + "'");

        final var keyOptions = new CreateOctKeyOptions(NAME)
                .setHardwareProtected(true)
                .setKeyOperations(KeyOperation.ENCRYPT, KeyOperation.WRAP_KEY, KeyOperation.DECRYPT, KeyOperation.UNWRAP_KEY);
        final var key = keyClient.createOctKey(keyOptions);
        Assertions.assertNotNull(key);
        Assertions.assertTrue(key.getId().startsWith(vaultUrl), "Key Id should start with vault URL '" + vaultUrl + "'");

        final var cryptographyClient = new CryptographyClientBuilder()
                .keyIdentifier(key.getId())
                .credential(credential)
                .httpClient(httpClient)
                .serviceVersion(CryptographyServiceVersion.V7_3)
                .disableChallengeResourceVerification()
                .retryPolicy(new RetryPolicy(new FixedDelay(0, Duration.ZERO)))
                .buildClient();

        final var testMessage = "test";
        final var encryptParameters = EncryptParameters.createA128CbcParameters(
                testMessage.getBytes(StandardCharsets.UTF_8), "iv-parameter-val".getBytes(StandardCharsets.UTF_8));
        final var encrypted = cryptographyClient
                .encrypt(encryptParameters, Context.NONE);
        final var decryptParameters = DecryptParameters
                .createA128CbcParameters(encrypted.getCipherText(), encrypted.getIv());
        final var decrypted = cryptographyClient
                .decrypt(decryptParameters, Context.NONE);

        Assertions.assertEquals(testMessage, new String(decrypted.getPlainText(), StandardCharsets.UTF_8));
    }

    protected void verifyTokenEndpointIsWorking(final String endpointUrl, final HttpClient httpClient) {
        httpClient.send(new HttpRequest(HttpMethod.GET, endpointUrl + "?resource=https://localhost"))
                .doOnError(Assertions::fail)
                .doOnSuccess(response -> Assertions.assertEquals(SUCCESS, response.getStatusCode()))
                .single()
                .block();
    }
}
