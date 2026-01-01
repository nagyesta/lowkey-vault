package com.github.nagyesta.lowkeyvault.context;

import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretServiceVersion;
import com.azure.security.keyvault.secrets.models.DeletedSecret;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.security.keyvault.secrets.models.SecretProperties;
import com.github.nagyesta.lowkeyvault.http.ApacheHttpClientProvider;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

public class SecretTestContext
        extends CommonTestContext<KeyVaultSecret, DeletedSecret, SecretProperties, SecretClient, SecretServiceVersion> {

    @Nullable
    private KeyVaultSecret createSecretOptions;

    private SecretServiceVersion secretServiceVersion = SecretServiceVersion.getLatest();

    public SecretServiceVersion getSecretServiceVersion() {
        return secretServiceVersion;
    }

    public void setApiVersion(final String version) {
        secretServiceVersion = Arrays.stream(SecretServiceVersion.values())
                .filter(v -> v.getVersion().equalsIgnoreCase(version)).findFirst().orElseThrow();
    }

    public SecretTestContext(final ApacheHttpClientProvider provider) {
        super(provider);
    }

    @Override
    protected SecretClient providerToClient(
            final ApacheHttpClientProvider provider,
            final SecretServiceVersion version) {
        return provider.getSecretClient(version);
    }

    public KeyVaultSecret getCreateSecretOptions() {
        return Objects.requireNonNull(createSecretOptions, "Create secret options cannot be null.");
    }

    public void setCreateSecretOptions(final KeyVaultSecret createSecretOptions) {
        this.createSecretOptions = createSecretOptions;
    }

    public void addFetchedSecret(
            final String name,
            final KeyVaultSecret secret) {
        addFetchedEntity(name, secret, keyVaultSecret -> keyVaultSecret.getProperties().getVersion());
    }

}
