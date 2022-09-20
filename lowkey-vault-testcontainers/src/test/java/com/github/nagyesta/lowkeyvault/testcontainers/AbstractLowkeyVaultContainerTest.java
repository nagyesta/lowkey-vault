package com.github.nagyesta.lowkeyvault.testcontainers;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.FixedDelay;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.SecretServiceVersion;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import org.junit.jupiter.api.Assertions;

import java.time.Duration;

public class AbstractLowkeyVaultContainerTest {

    private static final String NAME = "name";
    private static final String VALUE = "value";

    protected String getCurrentLowkeyVaultImageName() {
        return "lowkey-vault:" + System.getProperty("imageVersion");
    }

    protected void verifyConnectionIsWorking(final String vaultUrl, final HttpClient httpClient, final TokenCredential credential) {
        final SecretClient secretClient = new SecretClientBuilder()
                .vaultUrl(vaultUrl)
                .credential(credential)
                .httpClient(httpClient)
                .serviceVersion(SecretServiceVersion.V7_3)
                .disableChallengeResourceVerification()
                .retryPolicy(new RetryPolicy(new FixedDelay(0, Duration.ZERO)))
                .buildClient();

        final KeyVaultSecret secret = secretClient.setSecret(NAME, VALUE);

        Assertions.assertNotNull(secret);
    }
}
