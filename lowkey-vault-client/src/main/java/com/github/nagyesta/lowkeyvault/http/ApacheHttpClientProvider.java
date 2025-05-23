package com.github.nagyesta.lowkeyvault.http;

import com.azure.core.credential.BasicAuthenticationCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.FixedDelay;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.security.keyvault.certificates.CertificateAsyncClient;
import com.azure.security.keyvault.certificates.CertificateClient;
import com.azure.security.keyvault.certificates.CertificateClientBuilder;
import com.azure.security.keyvault.certificates.CertificateServiceVersion;
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
import lombok.Getter;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.TrustStrategy;

import javax.net.ssl.HostnameVerifier;
import java.net.URI;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 * Modified class based on <a href="https://github.com/Azure/azure-sdk-for-java/wiki/Custom-HTTP-Clients">Azure SDK wiki</a>.
 */
public final class ApacheHttpClientProvider {

    private static final String DUMMY = "dummy";

    @Getter
    private final String vaultUrl;
    private final UnaryOperator<URI> hostOverrideFunction;

    private final TrustStrategy trustStrategy;

    private final HostnameVerifier hostnameVerifier;

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
     * @see ApacheHttpRequest#ApacheHttpRequest(com.azure.core.http.HttpMethod,
     * java.net.URL, com.azure.core.http.HttpHeaders, UnaryOperator)
     */
    public ApacheHttpClientProvider(
            final String vaultUrl,
            final UnaryOperator<URI> hostOverrideFunction) {
        this(vaultUrl, hostOverrideFunction, null, null);
    }

    /**
     * Creates a new provider instance setting the vault URL and the host override function as well as SSL certificate
     * verification parameters.
     *
     * @param vaultUrl             The vault URL.
     * @param hostOverrideFunction The function mapping between the logical host name used by vault URLs
     *                             and the host name used by the host machine for accessing Lowkey Vault.
     *                             e.g. Maps from *.localhost:8443 to localhost:30443.
     * @param trustStrategy        The trust strategy we will use for SSL cert verification.
     *                             Defaults to {@link TrustSelfSignedStrategy} when null.
     * @param hostnameVerifier     The host name verifier we are using when the SSL certs are verified.
     *                             Defaults to {@link DefaultHostnameVerifier} when null.
     * @see ApacheHttpRequest#ApacheHttpRequest(com.azure.core.http.HttpMethod,
     * java.net.URL, com.azure.core.http.HttpHeaders, UnaryOperator)
     */
    public ApacheHttpClientProvider(
            final String vaultUrl,
            final UnaryOperator<URI> hostOverrideFunction,
            final TrustStrategy trustStrategy,
            final HostnameVerifier hostnameVerifier) {
        this.vaultUrl = vaultUrl;
        this.hostOverrideFunction = Optional.ofNullable(hostOverrideFunction)
                .orElse(uri -> uri);
        this.trustStrategy = Optional.ofNullable(trustStrategy).orElse(new TrustSelfSignedStrategy());
        this.hostnameVerifier = Optional.ofNullable(hostnameVerifier).orElse(new DefaultHostnameVerifier());
    }

    public HttpClient createInstance() {
        return new ApacheHttpClient(hostOverrideFunction, trustStrategy, hostnameVerifier);
    }

    public LowkeyVaultManagementClient getLowkeyVaultManagementClient(final ObjectMapper objectMapper) {
        return new LowkeyVaultManagementClientImpl(vaultUrl, createInstance(), objectMapper);
    }

    public KeyAsyncClient getKeyAsyncClient() {
        return getKeyAsyncClient(KeyServiceVersion.V7_4);
    }

    public KeyAsyncClient getKeyAsyncClient(final KeyServiceVersion version) {
        return getKeyBuilder().serviceVersion(version).buildAsyncClient();
    }

    public KeyClient getKeyClient() {
        return getKeyClient(KeyServiceVersion.V7_4);
    }

    public KeyClient getKeyClient(final KeyServiceVersion version) {
        return getKeyBuilder().serviceVersion(version).buildClient();
    }

    public CertificateAsyncClient getCertificateAsyncClient() {
        return getCertificateAsyncClient(CertificateServiceVersion.V7_4);
    }

    public CertificateAsyncClient getCertificateAsyncClient(final CertificateServiceVersion version) {
        return getCertificateBuilder().serviceVersion(version).buildAsyncClient();
    }

    public CertificateClient getCertificateClient() {
        return getCertificateClient(CertificateServiceVersion.V7_4);
    }

    public CertificateClient getCertificateClient(final CertificateServiceVersion version) {
        return getCertificateBuilder().serviceVersion(version).buildClient();
    }

    public SecretAsyncClient getSecretAsyncClient() {
        return getSecretAsyncClient(SecretServiceVersion.V7_4);
    }

    public SecretAsyncClient getSecretAsyncClient(final SecretServiceVersion version) {
        return getSecretBuilder().serviceVersion(version).buildAsyncClient();
    }

    public SecretClient getSecretClient() {
        return getSecretClient(SecretServiceVersion.V7_4);
    }

    public SecretClient getSecretClient(final SecretServiceVersion version) {
        return getSecretBuilder().serviceVersion(version).buildClient();
    }

    public CryptographyAsyncClient getCryptoAsyncClient(final String webKeyId) {
        return getCryptoAsyncClient(webKeyId, CryptographyServiceVersion.V7_4);
    }

    public CryptographyAsyncClient getCryptoAsyncClient(
            final String webKeyId,
            final CryptographyServiceVersion version) {
        return getCryptoBuilder(webKeyId).serviceVersion(version).buildAsyncClient();
    }

    public CryptographyClient getCryptoClient(final String webKeyId) {
        return getCryptoClient(webKeyId, CryptographyServiceVersion.V7_4);
    }

    public CryptographyClient getCryptoClient(
            final String webKeyId,
            final CryptographyServiceVersion version) {
        return getCryptoBuilder(webKeyId).serviceVersion(version).buildClient();
    }

    private KeyClientBuilder getKeyBuilder() {
        return new KeyClientBuilder()
                .vaultUrl(getVaultUrl())
                .credential(getCredential())
                .httpClient(createInstance())
                .disableChallengeResourceVerification()
                .retryPolicy(getRetryPolicy());
    }

    private CertificateClientBuilder getCertificateBuilder() {
        return new CertificateClientBuilder()
                .vaultUrl(getVaultUrl())
                .credential(getCredential())
                .httpClient(createInstance())
                .disableChallengeResourceVerification()
                .retryPolicy(getRetryPolicy());
    }

    private SecretClientBuilder getSecretBuilder() {
        return new SecretClientBuilder()
                .vaultUrl(getVaultUrl())
                .credential(getCredential())
                .httpClient(createInstance())
                .disableChallengeResourceVerification()
                .retryPolicy(getRetryPolicy());
    }

    private CryptographyClientBuilder getCryptoBuilder(final String webKeyId) {
        return new CryptographyClientBuilder()
                .keyIdentifier(Objects.requireNonNull(webKeyId))
                .credential(getCredential())
                .httpClient(createInstance())
                .disableChallengeResourceVerification()
                .retryPolicy(getRetryPolicy());
    }

    private RetryPolicy getRetryPolicy() {
        return new RetryPolicy(new FixedDelay(0, Duration.ZERO));
    }

    private BasicAuthenticationCredential getCredential() {
        return new BasicAuthenticationCredential(DUMMY, DUMMY);
    }

}
