package com.github.nagyesta.lowkeyvault.mapper.v7_2.key;

import com.github.nagyesta.lowkeyvault.mapper.common.registry.KeyConverterRegistry;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

import java.net.URI;

public class KeyEntityToV72KeyVersionItemModelConverter extends KeyEntityToV72KeyItemModelConverter {

    @Autowired
    public KeyEntityToV72KeyVersionItemModelConverter(@NonNull final KeyConverterRegistry registry) {
        super(registry);
    }

    @Override
    protected void register(final KeyConverterRegistry registry) {
        registry.registerVersionedItemConverter(this);
    }

    @Override
    protected String convertKeyId(
            final ReadOnlyKeyVaultKeyEntity source,
            final URI vaultUri) {
        return source.getId().asUri(vaultUri).toString();
    }
}
