package com.github.nagyesta.lowkeyvault.http;

import com.azure.core.credential.BasicAuthenticationCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.FixedDelay;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.security.keyvault.keys.KeyAsyncClient;
import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.KeyClientBuilder;
import com.azure.security.keyvault.keys.KeyServiceVersion;
import com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient;
import com.azure.security.keyvault.keys.cryptography.CryptographyClient;
import com.azure.security.keyvault.keys.cryptography.CryptographyClientBuilder;
import com.azure.security.keyvault.keys.cryptography.CryptographyServiceVersion;
import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.SecretServiceVersion;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nagyesta.lowkeyvault.http.management.LowkeyVaultManagementClient;
import com.github.nagyesta.lowkeyvault.http.management.impl.LowkeyVaultManagementClientImpl;

import java.net.URI;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Modified class based on https://github.com/Azure/azure-sdk-for-java/wiki/Custom-HTTP-Clients.
 */
public final class ApacheHttpClientProvider {

    private static final String DUMMY = "dummy";

    private final String vaultUrl;
    private final Function<URI, URI> hostOverrideFunction;

    public ApacheHttpClientProvider(final String vaultUrl) {
        this(vaultUrl, null);
    }

    /**
     * Creates a new provider instance setting the vault URL and the host override function.
     *
     * @param vaultUrl             The vault URL.
     * @param hostOverrideFunction The function mapping between the logical host name used by vault URLs
     *                             and the host name used by the host machine for accessing Lowkey Vault.
     *                             e.g. Maps from *.localhost:8443 to localhost:30443.
     * @see ApacheHttpRequest#ApacheHttpRequest(com.azure.core.http.HttpMethod, java.net.URL, com.azure.core.http.HttpHeaders, Function)
     */
    public ApacheHttpClientProvider(final String vaultUrl, final Function<URI, URI> hostOverrideFunction) {
        this.vaultUrl = vaultUrl;
        this.hostOverrideFunction = Optional.ofNullable(hostOverrideFunction)
                .orElse(Function.identity());
    }

    public HttpClient createInstance() {
        return new ApacheHttpClient(hostOverrideFunction);
    }

    public KeyAsyncClient getKeyAsyncClient() {
        return getKeyBuilder().buildAsyncClient();
    }

    public LowkeyVaultManagementClient getLowkeyVaultManagementClient(final ObjectMapper objectMapper) {
        return new LowkeyVaultManagementClientImpl(vaultUrl, createInstance(), objectMapper);
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
                .serviceVersion(KeyServiceVersion.V7_2)
                .retryPolicy(new RetryPolicy(new FixedDelay(0, Duration.ZERO)));
    }

    private SecretClientBuilder getSecretBuilder() {
        return new SecretClientBuilder()
                .vaultUrl(getVaultUrl())
                .credential(new BasicAuthenticationCredential(DUMMY, DUMMY))
                .httpClient(createInstance())
                .serviceVersion(SecretServiceVersion.V7_2)
                .retryPolicy(new RetryPolicy(new FixedDelay(0, Duration.ZERO)));
    }

    private CryptographyClientBuilder getCryptoBuilder(final String webKeyId) {
        return new CryptographyClientBuilder()
                .keyIdentifier(Objects.requireNonNull(webKeyId))
                .credential(new BasicAuthenticationCredential(DUMMY, DUMMY))
                .httpClient(createInstance())
                .serviceVersion(CryptographyServiceVersion.V7_2)
                .retryPolicy(new RetryPolicy(new FixedDelay(0, Duration.ZERO)));
    }

    public String getVaultUrl() {
        return vaultUrl;
    }

}
