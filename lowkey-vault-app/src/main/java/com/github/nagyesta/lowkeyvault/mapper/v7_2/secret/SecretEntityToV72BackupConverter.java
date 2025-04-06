package com.github.nagyesta.lowkeyvault.mapper.v7_2.secret;

import com.github.nagyesta.lowkeyvault.context.ApiVersionAware;
import com.github.nagyesta.lowkeyvault.mapper.common.AliasAwareConverter;
import com.github.nagyesta.lowkeyvault.mapper.common.BackupConverter;
import com.github.nagyesta.lowkeyvault.mapper.common.registry.SecretConverterRegistry;
import com.github.nagyesta.lowkeyvault.model.common.backup.SecretBackupListItem;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.SecretPropertiesModel;
import com.github.nagyesta.lowkeyvault.service.secret.ReadOnlyKeyVaultSecretEntity;
import com.github.nagyesta.lowkeyvault.service.secret.id.SecretEntityId;
import com.github.nagyesta.lowkeyvault.service.secret.id.VersionedSecretEntityId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

import java.util.SortedSet;

public class SecretEntityToV72BackupConverter
        extends BackupConverter<SecretEntityId, VersionedSecretEntityId, ReadOnlyKeyVaultSecretEntity,
        SecretPropertiesModel, SecretBackupListItem> {

    private final SecretConverterRegistry registry;

    @Autowired
    public SecretEntityToV72BackupConverter(@lombok.NonNull final SecretConverterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void afterPropertiesSet() {
        registry.registerBackupConverter(this);
    }

    @Override
    protected SecretBackupListItem convertUniqueFields(@NonNull final ReadOnlyKeyVaultSecretEntity source) {
        final var listItem = new SecretBackupListItem();
        listItem.setValue(source.getValue());
        listItem.setContentType(source.getContentType());
        return listItem;
    }

    @Override
    protected AliasAwareConverter<ReadOnlyKeyVaultSecretEntity, SecretPropertiesModel> propertiesConverter() {
        return registry.propertiesConverter(supportedVersions().last());
    }

    @Override
    public SortedSet<String> supportedVersions() {
        return ApiVersionAware.ALL_VERSIONS;
    }
}
