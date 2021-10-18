package com.github.nagyesta.lowkeyvault.mapper.v7_2.key;

import com.github.nagyesta.lowkeyvault.mapper.common.BaseEntityToV72PropertiesModelConverter;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyPropertiesModel;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class KeyEntityToV72PropertiesModelConverter
        extends BaseEntityToV72PropertiesModelConverter implements Converter<ReadOnlyKeyVaultKeyEntity, KeyPropertiesModel> {

    @Override
    @NonNull
    public KeyPropertiesModel convert(@org.springframework.lang.NonNull final ReadOnlyKeyVaultKeyEntity source) {
        return mapCommonFields(source, new KeyPropertiesModel());
    }

}
