package com.github.nagyesta.lowkeyvault.http;

import com.azure.core.credential.BasicAuthenticationCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.FixedDelay;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.security.keyvault.keys.KeyAsyncClient;
import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.KeyClientBuilder;

import java.time.Duration;

public final class ApacheHttpClientProvider {

    private static final String EMPTY = "";

    private final String vaultUrl;

    public ApacheHttpClientProvider(final String vaultUrl) {
        this.vaultUrl = vaultUrl;
    }

    public HttpClient createInstance() {
        return new ApacheHttpClient();
    }

    public KeyAsyncClient getKeyAsyncClient() {
        return getBuilder().buildAsyncClient();
    }

    public KeyClient getKeyClient() {
        return getBuilder().buildClient();
    }

    private KeyClientBuilder getBuilder() {
        return new KeyClientBuilder()
                .vaultUrl(vaultUrl)
                .credential(new BasicAuthenticationCredential(EMPTY, EMPTY))
                .httpClient(new ApacheHttpClient())
                .retryPolicy(new RetryPolicy(new FixedDelay(0, Duration.ZERO)));
    }

    public String getVaultUrl() {
        return vaultUrl;
    }
}
