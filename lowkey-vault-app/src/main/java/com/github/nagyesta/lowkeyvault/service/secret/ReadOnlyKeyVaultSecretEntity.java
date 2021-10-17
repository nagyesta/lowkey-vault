package com.github.nagyesta.lowkeyvault.service.secret;

import com.github.nagyesta.lowkeyvault.service.common.BaseVaultEntity;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyDeletedEntity;
import com.github.nagyesta.lowkeyvault.service.secret.id.VersionedSecretEntityId;

import java.net.URI;

public interface ReadOnlyKeyVaultSecretEntity
        extends BaseVaultEntity<VersionedSecretEntityId>, ReadOnlyDeletedEntity<VersionedSecretEntityId> {

    String getValue();

    String getContentType();

    VersionedSecretEntityId getId();

    URI getUri();

}
