package com.github.nagyesta.lowkeyvault.context;

import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.DeletedSecret;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.security.keyvault.secrets.models.SecretProperties;
import com.github.nagyesta.lowkeyvault.http.ApacheHttpClientProvider;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class SecretTestContext extends CommonTestContext<KeyVaultSecret, DeletedSecret, SecretProperties, SecretClient> {

    public static final OffsetDateTime NOW = OffsetDateTime.now(ZoneOffset.UTC);

    private KeyVaultSecret createSecretOptions;

    public SecretTestContext(final ApacheHttpClientProvider provider) {
        super(provider);
    }

    @Override
    protected SecretClient providerToClient(final ApacheHttpClientProvider provider) {
        return provider.getSecretClient();
    }

    public KeyVaultSecret getCreateSecretOptions() {
        return createSecretOptions;
    }

    public void setCreateSecretOptions(final KeyVaultSecret createSecretOptions) {
        this.createSecretOptions = createSecretOptions;
    }

    public void addFetchedSecret(final String name, final KeyVaultSecret secret) {
        addFetchedEntity(name, secret, keyVaultSecret -> keyVaultSecret.getProperties().getVersion());
    }

}
