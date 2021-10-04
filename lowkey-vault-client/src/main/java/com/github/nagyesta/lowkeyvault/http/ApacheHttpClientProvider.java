package com.github.nagyesta.lowkeyvault.http;

import com.azure.core.credential.BasicAuthenticationCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.FixedDelay;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.security.keyvault.keys.KeyAsyncClient;
import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.KeyClientBuilder;
import com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient;
import com.azure.security.keyvault.keys.cryptography.CryptographyClient;
import com.azure.security.keyvault.keys.cryptography.CryptographyClientBuilder;

import java.time.Duration;
import java.util.Objects;

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

    public CryptographyAsyncClient getCryptoAsyncClient(final String webKeyId) {
        return getCryptoBuilder(webKeyId).buildAsyncClient();
    }

    public CryptographyClient getCryptoClient(final String webKeyId) {
        return getCryptoBuilder(webKeyId).buildClient();
    }

    private KeyClientBuilder getBuilder() {
        return new KeyClientBuilder()
                .vaultUrl(getVaultUrl())
                .credential(new BasicAuthenticationCredential(EMPTY, EMPTY))
                .httpClient(createInstance())
                .retryPolicy(new RetryPolicy(new FixedDelay(0, Duration.ZERO)));
    }

    private CryptographyClientBuilder getCryptoBuilder(final String webKeyId) {
        return new CryptographyClientBuilder()
                .keyIdentifier(Objects.requireNonNull(ClientUriUtil.hackPort(webKeyId)))
                .credential(new BasicAuthenticationCredential(EMPTY, EMPTY))
                .httpClient(createInstance())
                .retryPolicy(new RetryPolicy(new FixedDelay(0, Duration.ZERO)));
    }

    public String getVaultUrl() {
        return vaultUrl;
    }
}
