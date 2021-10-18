package com.github.nagyesta.lowkeyvault.mapper.v7_2.secret;

import com.github.nagyesta.lowkeyvault.mapper.common.BaseEntityToV72PropertiesModelConverter;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.SecretPropertiesModel;
import com.github.nagyesta.lowkeyvault.service.secret.ReadOnlyKeyVaultSecretEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class SecretEntityToV72PropertiesModelConverter
        extends BaseEntityToV72PropertiesModelConverter implements Converter<ReadOnlyKeyVaultSecretEntity, SecretPropertiesModel> {

    @Override
    @NonNull
    public SecretPropertiesModel convert(@NonNull final ReadOnlyKeyVaultSecretEntity source) {
        return mapCommonFields(source, new SecretPropertiesModel());
    }

}
