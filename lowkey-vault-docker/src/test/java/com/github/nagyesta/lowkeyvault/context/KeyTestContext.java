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
import org.jspecify.annotations.Nullable;

import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class KeyTestContext extends CommonTestContext<KeyVaultKey, DeletedKey, KeyProperties, KeyClient, KeyServiceVersion> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Nullable
    private LowkeyVaultManagementClient lowkeyVaultManagementClient;
    @Nullable
    private CryptographyClient cryptographyClient;
    @Nullable
    private CreateRsaKeyOptions createRsaKeyOptions;
    @Nullable
    private CreateEcKeyOptions createEcKeyOptions;
    @Nullable
    private CreateOctKeyOptions createOctKeyOptions;
    private KeyOperation @Nullable [] updateKeyOperations;
    @Nullable
    private EncryptResult encryptResult;
    @Nullable
    private DecryptResult decryptResult;
    private byte @Nullable [] signatureResult;
    @Nullable
    private Boolean verifyResult;
    @Nullable
    private KeyPair keyPair;
    @Nullable
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
    protected KeyClient providerToClient(
            final ApacheHttpClientProvider provider,
            final KeyServiceVersion version) {
        return provider.getKeyClient(version);
    }

    public synchronized LowkeyVaultManagementClient getLowkeyVaultManagementClient() {
        if (lowkeyVaultManagementClient == null) {
            lowkeyVaultManagementClient = getProvider().getLowkeyVaultManagementClient(objectMapper);
        }
        return lowkeyVaultManagementClient;
    }

    public CryptographyClient getCryptographyClient() {
        return Objects.requireNonNull(cryptographyClient, "Cryptography client cannot be null.");
    }

    public void setCryptographyClient(final CryptographyClient cryptographyClient) {
        this.cryptographyClient = cryptographyClient;
    }

    @Nullable
    public CreateKeyOptions getCreateKeyOptions() {
        return Optional.<CreateKeyOptions>ofNullable(createRsaKeyOptions)
                .orElse(Optional.<CreateKeyOptions>ofNullable(createEcKeyOptions)
                        .orElse(createOctKeyOptions));
    }

    @Nullable
    public CreateRsaKeyOptions getCreateRsaKeyOptions() {
        return createRsaKeyOptions;
    }

    public void setCreateRsaKeyOptions(final CreateRsaKeyOptions createRsaKeyOptions) {
        this.createRsaKeyOptions = createRsaKeyOptions;
    }

    @Nullable
    public CreateEcKeyOptions getCreateEcKeyOptions() {
        return createEcKeyOptions;
    }

    public void setCreateEcKeyOptions(final CreateEcKeyOptions createEcKeyOptions) {
        this.createEcKeyOptions = createEcKeyOptions;
    }

    @Nullable
    public CreateOctKeyOptions getCreateOctKeyOptions() {
        return createOctKeyOptions;
    }

    public void setCreateOctKeyOptions(final CreateOctKeyOptions createoctKeyOptions) {
        this.createOctKeyOptions = createoctKeyOptions;
    }

    public void addFetchedKey(
            final String name,
            final KeyVaultKey key) {
        addFetchedEntity(name, key, keyVaultKey -> keyVaultKey.getProperties().getVersion());
    }

    public KeyOperation[] getUpdateKeyOperations() {
        return Objects.requireNonNull(updateKeyOperations, "Update operations cannot be null.");
    }

    public void setUpdateKeyOperations(final KeyOperation[] updateKeyOperations) {
        this.updateKeyOperations = updateKeyOperations;
    }

    public EncryptResult getEncryptResult() {
        return Objects.requireNonNull(encryptResult, "Encrypt result cannot be null.");
    }

    public void setEncryptResult(final EncryptResult encryptResult) {
        this.encryptResult = encryptResult;
    }

    public DecryptResult getDecryptResult() {
        return Objects.requireNonNull(decryptResult, "Decrypt result cannot be null.");
    }

    public void setDecryptResult(final DecryptResult decryptResult) {
        this.decryptResult = decryptResult;
    }

    public byte[] getSignatureResult() {
        return Objects.requireNonNull(signatureResult, "Signature result cannot be null.");
    }

    public void setSignatureResult(final byte[] signatureResult) {
        this.signatureResult = signatureResult;
    }

    public Boolean getVerifyResult() {
        return Objects.requireNonNull(verifyResult, "Verify result cannot be null.");
    }

    public void setVerifyResult(final Boolean verifyResult) {
        this.verifyResult = verifyResult;
    }

    public KeyPair getKeyPair() {
        return Objects.requireNonNull(keyPair, "Key pair cannot be null.");
    }

    public void setKeyPair(final KeyPair keyPair) {
        this.keyPair = keyPair;
    }

    public SecretKey getSecretKey() {
        return Objects.requireNonNull(secretKey, "Secret key cannot be null.");
    }

    public void setSecretKey(final SecretKey secretKey) {
        this.secretKey = secretKey;
    }
}
