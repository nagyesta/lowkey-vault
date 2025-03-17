package com.github.nagyesta.lowkeyvault.testcontainers;

import com.azure.core.credential.BasicAuthenticationCredential;
import com.azure.security.keyvault.certificates.CertificateClientBuilder;
import com.azure.security.keyvault.keys.KeyClientBuilder;
import com.azure.security.keyvault.keys.cryptography.CryptographyClientBuilder;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nagyesta.lowkeyvault.http.ApacheHttpClient;
import com.github.nagyesta.lowkeyvault.http.AuthorityOverrideFunction;
import com.github.nagyesta.lowkeyvault.http.management.LowkeyVaultManagementClient;
import com.github.nagyesta.lowkeyvault.http.management.impl.LowkeyVaultManagementClientImpl;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LowkeyVaultClientFactory {

    private final LowkeyVaultContainer lowkeyVaultContainer;
    private final Map<String, ApacheHttpClient> httpClientCache = new ConcurrentHashMap<>();

    public LowkeyVaultClientFactory(final LowkeyVaultContainer lowkeyVaultContainer) {
        this.lowkeyVaultContainer = lowkeyVaultContainer;
    }

    public BasicAuthenticationCredential getBasicAuthenticationCredential() {
        return new BasicAuthenticationCredential(lowkeyVaultContainer.getUsername(), lowkeyVaultContainer.getPassword());
    }

    public SecretClientBuilder getSecretClientBuilderForDefaultVault() {
        return getSecretClientBuilderFor(URI.create(lowkeyVaultContainer.getDefaultVaultBaseUrl()));
    }

    public SecretClientBuilder getSecretClientBuilderFor(final String vaultName) {
        return getSecretClientBuilderFor(URI.create(lowkeyVaultContainer.getVaultBaseUrl(vaultName)));
    }

    public SecretClientBuilder getSecretClientBuilderFor(final URI vaultUri) {
        return new SecretClientBuilder()
                .vaultUrl(vaultUri.toString())
                .credential(getBasicAuthenticationCredential())
                .disableChallengeResourceVerification()
                .httpClient(getApacheHttpClient(vaultUri.getAuthority()));
    }

    public KeyClientBuilder getKeyClientBuilderForDefaultVault() {
        return getKeyClientBuilderFor(URI.create(lowkeyVaultContainer.getDefaultVaultBaseUrl()));
    }

    public KeyClientBuilder getKeyClientBuilderFor(final String vaultName) {
        return getKeyClientBuilderFor(URI.create(lowkeyVaultContainer.getVaultBaseUrl(vaultName)));
    }

    public KeyClientBuilder getKeyClientBuilderFor(final URI vaultUri) {
        return new KeyClientBuilder()
                .vaultUrl(vaultUri.toString())
                .credential(getBasicAuthenticationCredential())
                .disableChallengeResourceVerification()
                .httpClient(getApacheHttpClient(vaultUri.getAuthority()));
    }

    public CryptographyClientBuilder getCryptoClientBuilderForDefaultVault() {
        return getCryptoClientBuilderFor(URI.create(lowkeyVaultContainer.getDefaultVaultBaseUrl()));
    }

    public CryptographyClientBuilder getCryptoClientBuilderFor(final String vaultName) {
        return getCryptoClientBuilderFor(URI.create(lowkeyVaultContainer.getVaultBaseUrl(vaultName)));
    }

    public CryptographyClientBuilder getCryptoClientBuilderFor(final URI vaultUri) {
        return new CryptographyClientBuilder()
                .credential(getBasicAuthenticationCredential())
                .disableChallengeResourceVerification()
                .httpClient(getApacheHttpClient(vaultUri.getAuthority()));
    }

    public CertificateClientBuilder getCertificateClientBuilderForDefaultVault() {
        return getCertificateClientBuilderFor(URI.create(lowkeyVaultContainer.getDefaultVaultBaseUrl()));
    }

    public CertificateClientBuilder getCertificateClientBuilderFor(final String vaultName) {
        return getCertificateClientBuilderFor(URI.create(lowkeyVaultContainer.getVaultBaseUrl(vaultName)));
    }

    public CertificateClientBuilder getCertificateClientBuilderFor(final URI vaultUri) {
        return new CertificateClientBuilder()
                .vaultUrl(vaultUri.toString())
                .credential(getBasicAuthenticationCredential())
                .disableChallengeResourceVerification()
                .httpClient(getApacheHttpClient(vaultUri.getAuthority()));
    }

    public LowkeyVaultManagementClient getLowkeyVaultManagementClient() {
        return new LowkeyVaultManagementClientImpl(
                "https://" + lowkeyVaultContainer.getEndpointAuthority(),
                getApacheHttpClient(lowkeyVaultContainer.getEndpointAuthority()),
                new ObjectMapper()
        );
    }

    private ApacheHttpClient getApacheHttpClient(final String vaultAuthority) {
        return httpClientCache.computeIfAbsent(vaultAuthority, v -> new ApacheHttpClient(
                new AuthorityOverrideFunction(v, lowkeyVaultContainer.getEndpointAuthority()),
                new TrustSelfSignedStrategy(),
                new DefaultHostnameVerifier()
        ));
    }
}
