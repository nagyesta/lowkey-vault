package com.github.nagyesta.lowkeyvault.mapper.common.registry;

import com.github.nagyesta.lowkeyvault.mapper.common.BaseEntityConverterRegistry;
import com.github.nagyesta.lowkeyvault.model.common.backup.SecretBackupList;
import com.github.nagyesta.lowkeyvault.model.common.backup.SecretBackupListItem;
import com.github.nagyesta.lowkeyvault.model.common.backup.SecretBackupModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.*;
import com.github.nagyesta.lowkeyvault.service.secret.ReadOnlyKeyVaultSecretEntity;
import com.github.nagyesta.lowkeyvault.service.secret.id.SecretEntityId;
import com.github.nagyesta.lowkeyvault.service.secret.id.VersionedSecretEntityId;
import lombok.EqualsAndHashCode;

import java.net.URI;

@EqualsAndHashCode(callSuper = true)
public class SecretConverterRegistry extends BaseEntityConverterRegistry<SecretEntityId, VersionedSecretEntityId,
        ReadOnlyKeyVaultSecretEntity, KeyVaultSecretModel, DeletedKeyVaultSecretModel, SecretPropertiesModel, KeyVaultSecretItemModel,
        DeletedKeyVaultSecretItemModel, SecretBackupListItem, SecretBackupList, SecretBackupModel> {

    @Override
    public SecretEntityId entityId(final URI baseUri, final String name) {
        return new SecretEntityId(baseUri, name);
    }

    @Override
    public VersionedSecretEntityId versionedEntityId(final URI baseUri, final String name, final String version) {
        return new VersionedSecretEntityId(baseUri, name, version);
    }
}
