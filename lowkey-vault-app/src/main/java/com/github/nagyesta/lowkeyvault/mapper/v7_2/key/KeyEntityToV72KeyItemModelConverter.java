package com.github.nagyesta.lowkeyvault.mapper.v7_2.key;

import com.github.nagyesta.lowkeyvault.mapper.common.BaseRecoveryAwareConverter;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.DeletedKeyVaultKeyItemModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyVaultKeyItemModel;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class KeyEntityToV72KeyItemModelConverter
        extends BaseRecoveryAwareConverter<VersionedKeyEntityId,
        ReadOnlyKeyVaultKeyEntity, KeyVaultKeyItemModel, DeletedKeyVaultKeyItemModel> {

    private final KeyEntityToV72PropertiesModelConverter keyEntityToV72PropertiesModelConverter;

    @Autowired
    public KeyEntityToV72KeyItemModelConverter(
            @NonNull final KeyEntityToV72PropertiesModelConverter keyEntityToV72PropertiesModelConverter) {
        super(KeyVaultKeyItemModel::new, DeletedKeyVaultKeyItemModel::new);
        this.keyEntityToV72PropertiesModelConverter = keyEntityToV72PropertiesModelConverter;
    }

    @Override
    protected <M extends KeyVaultKeyItemModel> M mapActiveFields(final ReadOnlyKeyVaultKeyEntity source, final M model) {
        model.setKeyId(convertKeyId(source));
        model.setAttributes(keyEntityToV72PropertiesModelConverter.convert(source));
        model.setTags(source.getTags());
        return model;
    }

    protected String convertKeyId(final ReadOnlyKeyVaultKeyEntity source) {
        return source.getId().asUriNoVersion().toString();
    }
}
