package com.github.nagyesta.lowkeyvault.context;

import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.KeyServiceVersion;
import com.azure.security.keyvault.keys.cryptography.CryptographyClient;
import com.azure.security.keyvault.keys.cryptography.CryptographyServiceVersion;
import com.azure.security.keyvault.keys.cryptography.models.DecryptResult;
import com.azure.security.keyvault.keys.cryptography.models.EncryptResult;
import com.azure.security.keyvault.keys.models.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nagyesta.lowkeyvault.http.ApacheHttpClientProvider;
import com.github.nagyesta.lowkeyvault.http.management.LowkeyVaultManagementClient;

import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.util.Arrays;
import java.util.Optional;

public class KeyTestContext extends CommonTestContext<KeyVaultKey, DeletedKey, KeyProperties, KeyClient, KeyServiceVersion> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private LowkeyVaultManagementClient lowkeyVaultManagementClient;
    private CryptographyClient cryptographyClient;
    private CreateRsaKeyOptions createRsaKeyOptions;
    private CreateEcKeyOptions createEcKeyOptions;
    private CreateOctKeyOptions createOctKeyOptions;
    private KeyOperation[] updateKeyOperations;
    private EncryptResult encryptResult;
    private DecryptResult decryptResult;
    private byte[] signatureResult;
    private Boolean verifyResult;
    private KeyPair keyPair;
    private SecretKey secretKey;
    private KeyServiceVersion keyServiceVersion = KeyServiceVersion.getLatest();
    private CryptographyServiceVersion cryptoServiceVersion = CryptographyServiceVersion.getLatest();

    public KeyTestContext(final ApacheHttpClientProvider provider) {
        super(provider);
    }

    public void setApiVersion(final String version) {
        keyServiceVersion = Arrays.stream(KeyServiceVersion.values())
                .filter(v -> v.getVersion().equalsIgnoreCase(version)).findFirst().orElseThrow();
        cryptoServiceVersion = Arrays.stream(CryptographyServiceVersion.values())
                .filter(v -> v.getVersion().equalsIgnoreCase(version)).findFirst().orElseThrow();
    }

    public KeyServiceVersion getKeyServiceVersion() {
        return keyServiceVersion;
    }

    public CryptographyServiceVersion getCryptoServiceVersion() {
        return cryptoServiceVersion;
    }

    @Override
    protected KeyClient providerToClient(final ApacheHttpClientProvider provider, final KeyServiceVersion version) {
        return provider.getKeyClient(version);
    }

    public synchronized LowkeyVaultManagementClient getLowkeyVaultManagementClient() {
        if (lowkeyVaultManagementClient == null) {
            lowkeyVaultManagementClient = getProvider().getLowkeyVaultManagementClient(objectMapper);
        }
        return lowkeyVaultManagementClient;
    }

    public CryptographyClient getCryptographyClient() {
        return cryptographyClient;
    }

    public void setCryptographyClient(final CryptographyClient cryptographyClient) {
        this.cryptographyClient = cryptographyClient;
    }

    public CreateKeyOptions getCreateKeyOptions() {
        return Optional.<CreateKeyOptions>ofNullable(createRsaKeyOptions)
                .orElse(Optional.<CreateKeyOptions>ofNullable(createEcKeyOptions).orElse(createOctKeyOptions));
    }

    public CreateRsaKeyOptions getCreateRsaKeyOptions() {
        return createRsaKeyOptions;
    }

    public void setCreateRsaKeyOptions(final CreateRsaKeyOptions createRsaKeyOptions) {
        this.createRsaKeyOptions = createRsaKeyOptions;
    }

    public CreateEcKeyOptions getCreateEcKeyOptions() {
        return createEcKeyOptions;
    }

    public void setCreateEcKeyOptions(final CreateEcKeyOptions createEcKeyOptions) {
        this.createEcKeyOptions = createEcKeyOptions;
    }

    public CreateOctKeyOptions getCreateOctKeyOptions() {
        return createOctKeyOptions;
    }

    public void setCreateOctKeyOptions(final CreateOctKeyOptions createoctKeyOptions) {
        this.createOctKeyOptions = createoctKeyOptions;
    }

    public void addFetchedKey(final String name, final KeyVaultKey key) {
        addFetchedEntity(name, key, keyVaultKey -> keyVaultKey.getProperties().getVersion());
    }

    public KeyOperation[] getUpdateKeyOperations() {
        return updateKeyOperations;
    }

    public void setUpdateKeyOperations(final KeyOperation[] updateKeyOperations) {
        this.updateKeyOperations = updateKeyOperations;
    }

    public EncryptResult getEncryptResult() {
        return encryptResult;
    }

    public void setEncryptResult(final EncryptResult encryptResult) {
        this.encryptResult = encryptResult;
    }

    public DecryptResult getDecryptResult() {
        return decryptResult;
    }

    public void setDecryptResult(final DecryptResult decryptResult) {
        this.decryptResult = decryptResult;
    }

    public byte[] getSignatureResult() {
        return signatureResult;
    }

    public void setSignatureResult(final byte[] signatureResult) {
        this.signatureResult = signatureResult;
    }

    public Boolean getVerifyResult() {
        return verifyResult;
    }

    public void setVerifyResult(final Boolean verifyResult) {
        this.verifyResult = verifyResult;
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }

    public void setKeyPair(final KeyPair keyPair) {
        this.keyPair = keyPair;
    }

    public SecretKey getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(final SecretKey secretKey) {
        this.secretKey = secretKey;
    }
}
