package com.github.nagyesta.lowkeyvault.mapper.v7_2.key;

import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public class KeyEntityToV72KeyVersionItemModelConverter
        extends KeyEntityToV72KeyItemModelConverter {

    @Autowired
    public KeyEntityToV72KeyVersionItemModelConverter(
            @NonNull final KeyEntityToV72PropertiesModelConverter keyEntityToV72PropertiesModelConverter) {
        super(keyEntityToV72PropertiesModelConverter);
    }

    @Override
    protected String convertKeyId(final ReadOnlyKeyVaultKeyEntity source, final URI vaultUri) {
        return source.getId().asUri(vaultUri).toString();
    }
}
