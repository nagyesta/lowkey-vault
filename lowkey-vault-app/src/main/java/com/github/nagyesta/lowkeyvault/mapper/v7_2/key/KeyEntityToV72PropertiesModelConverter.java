package com.github.nagyesta.lowkeyvault.mapper.v7_2.key;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyPropertiesModel;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class KeyEntityToV72PropertiesModelConverter implements Converter<ReadOnlyKeyVaultKeyEntity, KeyPropertiesModel> {

    @Override
    @NonNull
    public KeyPropertiesModel convert(@org.springframework.lang.NonNull final ReadOnlyKeyVaultKeyEntity source) {
        return mapAttributes(source);
    }

    private KeyPropertiesModel mapAttributes(final ReadOnlyKeyVaultKeyEntity entity) {
        final KeyPropertiesModel attributes = new KeyPropertiesModel();
        attributes.setCreatedOn(entity.getCreated());
        attributes.setUpdatedOn(entity.getUpdated());
        attributes.setEnabled(entity.isEnabled());
        entity.getExpiry().ifPresent(attributes::setExpiresOn);
        entity.getNotBefore().ifPresent(attributes::setNotBefore);
        attributes.setRecoveryLevel(entity.getRecoveryLevel());
        attributes.setRecoverableDays(entity.getRecoverableDays());
        return attributes;
    }
}
