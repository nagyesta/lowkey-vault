package com.github.nagyesta.lowkeyvault.mapper.v7_2.key;

import com.github.nagyesta.lowkeyvault.mapper.AliasAwareConverter;
import com.github.nagyesta.lowkeyvault.mapper.common.BaseEntityToV72PropertiesModelConverter;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyPropertiesModel;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public class KeyEntityToV72PropertiesModelConverter
        extends BaseEntityToV72PropertiesModelConverter implements AliasAwareConverter<ReadOnlyKeyVaultKeyEntity, KeyPropertiesModel> {

    @Override
    @NonNull
    public KeyPropertiesModel convert(
            @org.springframework.lang.NonNull final ReadOnlyKeyVaultKeyEntity source,
            @org.springframework.lang.NonNull final URI vaultUri) {
        return mapCommonFields(source, new KeyPropertiesModel());
    }

}
