package com.github.nagyesta.lowkeyvault.mapper.v7_2.secret;

import com.github.nagyesta.lowkeyvault.service.secret.ReadOnlyKeyVaultSecretEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public class SecretEntityToV72SecretVersionItemModelConverter
        extends SecretEntityToV72SecretItemModelConverter {

    @Autowired
    public SecretEntityToV72SecretVersionItemModelConverter(
            @NonNull final SecretEntityToV72PropertiesModelConverter secretEntityToV72PropertiesModelConverter) {
        super(secretEntityToV72PropertiesModelConverter);
    }

    @Override
    protected String convertSecretId(final ReadOnlyKeyVaultSecretEntity source, final URI vaultUri) {
        return source.getId().asUri(vaultUri).toString();
    }
}
