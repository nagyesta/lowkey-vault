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
import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;

import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public final class ApacheHttpClientProvider {

    private static final String DUMMY = "dummy";

    private final String vaultUrl;
    private final Set<String> hostOverride;

    public ApacheHttpClientProvider(final String vaultUrl) {
        this(vaultUrl, true);
    }

    public ApacheHttpClientProvider(final String vaultUrl, final boolean forceLocalhost) {
        this.vaultUrl = vaultUrl;
        if (forceLocalhost) {
            this.hostOverride = Set.of(URI.create(vaultUrl).getHost());
        } else {
            this.hostOverride = Collections.emptySet();
        }
    }

    public HttpClient createInstance() {
        return new ApacheHttpClient(hostOverride);
    }

    public KeyAsyncClient getKeyAsyncClient() {
        return getKeyBuilder().buildAsyncClient();
    }

    public KeyClient getKeyClient() {
        return getKeyBuilder().buildClient();
    }

    public SecretAsyncClient getSecretAsyncClient() {
        return getSecretBuilder().buildAsyncClient();
    }

    public SecretClient getSecretClient() {
        return getSecretBuilder().buildClient();
    }

    public CryptographyAsyncClient getCryptoAsyncClient(final String webKeyId) {
        return getCryptoBuilder(webKeyId).buildAsyncClient();
    }

    public CryptographyClient getCryptoClient(final String webKeyId) {
        return getCryptoBuilder(webKeyId).buildClient();
    }

    private KeyClientBuilder getKeyBuilder() {
        return new KeyClientBuilder()
                .vaultUrl(getVaultUrl())
                .credential(new BasicAuthenticationCredential(DUMMY, DUMMY))
                .httpClient(createInstance())
                .retryPolicy(new RetryPolicy(new FixedDelay(0, Duration.ZERO)));
    }

    private SecretClientBuilder getSecretBuilder() {
        return new SecretClientBuilder()
                .vaultUrl(getVaultUrl())
                .credential(new BasicAuthenticationCredential(DUMMY, DUMMY))
                .httpClient(createInstance())
                .retryPolicy(new RetryPolicy(new FixedDelay(0, Duration.ZERO)));
    }

    private CryptographyClientBuilder getCryptoBuilder(final String webKeyId) {
        return new CryptographyClientBuilder()
                .keyIdentifier(Objects.requireNonNull(webKeyId))
                .credential(new BasicAuthenticationCredential(DUMMY, DUMMY))
                .httpClient(createInstance())
                .retryPolicy(new RetryPolicy(new FixedDelay(0, Duration.ZERO)));
    }

    public String getVaultUrl() {
        return vaultUrl;
    }
}
