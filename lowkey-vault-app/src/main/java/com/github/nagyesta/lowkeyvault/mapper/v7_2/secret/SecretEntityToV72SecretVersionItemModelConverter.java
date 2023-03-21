package com.github.nagyesta.lowkeyvault.mapper.v7_2.secret;

import com.github.nagyesta.lowkeyvault.mapper.common.registry.SecretConverterRegistry;
import com.github.nagyesta.lowkeyvault.service.secret.ReadOnlyKeyVaultSecretEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

import java.net.URI;

public class SecretEntityToV72SecretVersionItemModelConverter
        extends SecretEntityToV72SecretItemModelConverter {

    @Autowired
    public SecretEntityToV72SecretVersionItemModelConverter(@NonNull final SecretConverterRegistry registry) {
        super(registry);
    }

    protected void register(final SecretConverterRegistry registry) {
        registry.registerVersionedItemConverter(this);
    }

    @Override
    protected String convertSecretId(final ReadOnlyKeyVaultSecretEntity source, final URI vaultUri) {
        return source.getId().asUri(vaultUri).toString();
    }
}
