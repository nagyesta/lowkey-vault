package com.github.nagyesta.lowkeyvault.mapper.v7_2.key;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyPropertiesModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyVaultKeyItemModel;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class KeyEntityToV72ItemModelConverter implements Converter<ReadOnlyKeyVaultKeyEntity, KeyVaultKeyItemModel> {

    private final KeyEntityToV72PropertiesModelConverter keyEntityToV72PropertiesModelConverter;

    @Autowired
    public KeyEntityToV72ItemModelConverter(@NonNull final KeyEntityToV72PropertiesModelConverter keyEntityToV72PropertiesModelConverter) {
        this.keyEntityToV72PropertiesModelConverter = keyEntityToV72PropertiesModelConverter;
    }

    @Override
    @org.springframework.lang.NonNull
    public KeyVaultKeyItemModel convert(final ReadOnlyKeyVaultKeyEntity source) {
        final KeyPropertiesModel attributes = keyEntityToV72PropertiesModelConverter.convert(source);
        final Map<String, String> tags = source.getTags();
        return new KeyVaultKeyItemModel(attributes, source.getUri(), tags);
    }
}
