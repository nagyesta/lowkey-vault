package com.github.nagyesta.lowkeyvault.context;

import com.azure.security.keyvault.certificates.CertificateClient;
import com.azure.security.keyvault.certificates.CertificateServiceVersion;
import com.azure.security.keyvault.certificates.models.CertificatePolicy;
import com.azure.security.keyvault.certificates.models.CertificateProperties;
import com.azure.security.keyvault.certificates.models.DeletedCertificate;
import com.azure.security.keyvault.certificates.models.KeyVaultCertificate;
import com.azure.security.keyvault.keys.models.KeyOperation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nagyesta.lowkeyvault.http.ApacheHttpClientProvider;
import com.github.nagyesta.lowkeyvault.http.management.LowkeyVaultManagementClient;

import java.security.KeyPair;
import java.util.Arrays;
import java.util.Map;

public class CertificateTestContext extends
        CommonTestContext<KeyVaultCertificate, DeletedCertificate, CertificateProperties, CertificateClient, CertificateServiceVersion> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private LowkeyVaultManagementClient lowkeyVaultManagementClient;
    private KeyOperation[] updateKeyOperations;
    private KeyPair keyPair;
    private CertificateServiceVersion certificateServiceVersion = CertificateServiceVersion.getLatest();
    private Map<String, String> tags;
    private CertificatePolicy policy;
    private CertificatePolicy downloadedPolicy;

    public CertificateTestContext(final ApacheHttpClientProvider provider) {
        super(provider);
    }

    public void setApiVersion(final String version) {
        certificateServiceVersion = Arrays.stream(CertificateServiceVersion.values())
                .filter(v -> v.getVersion().equalsIgnoreCase(version)).findFirst().orElseThrow();
    }

    public CertificateServiceVersion getCertificateServiceVersion() {
        return certificateServiceVersion;
    }

    @Override
    protected CertificateClient providerToClient(final ApacheHttpClientProvider provider, final CertificateServiceVersion version) {
        return provider.getCertificateClient(version);
    }

    public synchronized LowkeyVaultManagementClient getLowkeyVaultManagementClient() {
        if (lowkeyVaultManagementClient == null) {
            lowkeyVaultManagementClient = getProvider().getLowkeyVaultManagementClient(objectMapper);
        }
        return lowkeyVaultManagementClient;
    }

    public void addFetchedCertificate(final String name, final KeyVaultCertificate certificate) {
        addFetchedEntity(name, certificate, keyVaultCertificate -> keyVaultCertificate.getProperties().getVersion());
    }

    public KeyOperation[] getUpdateKeyOperations() {
        return updateKeyOperations;
    }

    public void setUpdateKeyOperations(final KeyOperation[] updateKeyOperations) {
        this.updateKeyOperations = updateKeyOperations;
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }

    public void setKeyPair(final KeyPair keyPair) {
        this.keyPair = keyPair;
    }

    public CertificatePolicy getPolicy() {
        return policy;
    }

    public void setPolicy(final CertificatePolicy policy) {
        this.policy = policy;
    }

    public CertificatePolicy getDownloadedPolicy() {
        return downloadedPolicy;
    }

    public void setDownloadedPolicy(final CertificatePolicy downloadedPolicy) {
        this.downloadedPolicy = downloadedPolicy;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(final Map<String, String> tags) {
        this.tags = tags;
    }
}
