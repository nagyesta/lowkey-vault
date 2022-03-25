package com.github.nagyesta.lowkeyvault.model.v7_2.secret;

import com.github.nagyesta.lowkeyvault.mapper.common.BackupConverter;
import com.github.nagyesta.lowkeyvault.service.secret.ReadOnlyKeyVaultSecretEntity;
import com.github.nagyesta.lowkeyvault.service.secret.id.VersionedSecretEntityId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class SecretEntityToV72BackupConverter
        extends BackupConverter<VersionedSecretEntityId, ReadOnlyKeyVaultSecretEntity, SecretPropertiesModel, SecretBackupListItem> {

    @Autowired
    public SecretEntityToV72BackupConverter(
            @NonNull final Converter<ReadOnlyKeyVaultSecretEntity, SecretPropertiesModel> propertiesConverter) {
        super(propertiesConverter);
    }

    @Override
    protected SecretBackupListItem convertUniqueFields(@NonNull final ReadOnlyKeyVaultSecretEntity source) {
        final SecretBackupListItem listItem = new SecretBackupListItem();
        listItem.setValue(source.getValue());
        listItem.setContentType(source.getContentType());
        return listItem;
    }
}
