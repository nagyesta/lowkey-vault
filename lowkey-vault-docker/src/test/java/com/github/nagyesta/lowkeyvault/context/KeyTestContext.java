package com.github.nagyesta.lowkeyvault.context;

import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.cryptography.CryptographyClient;
import com.azure.security.keyvault.keys.cryptography.models.DecryptResult;
import com.azure.security.keyvault.keys.cryptography.models.EncryptResult;
import com.azure.security.keyvault.keys.models.*;
import com.github.nagyesta.lowkeyvault.http.ApacheHttpClientProvider;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

public class KeyTestContext extends CommonTestContext<KeyVaultKey, DeletedKey, KeyProperties, KeyClient> {

    public static final OffsetDateTime NOW = OffsetDateTime.now(ZoneOffset.UTC);

    private CryptographyClient cryptographyClient;
    private CreateRsaKeyOptions createRsaKeyOptions;
    private CreateEcKeyOptions createEcKeyOptions;
    private CreateOctKeyOptions createOctKeyOptions;
    private KeyOperation[] updateKeyOperations;
    private EncryptResult encryptResult;
    private DecryptResult decryptResult;

    public KeyTestContext(final ApacheHttpClientProvider provider) {
        super(provider);
    }

    @Override
    protected KeyClient providerToClient(final ApacheHttpClientProvider provider) {
        return provider.getKeyClient();
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
}
