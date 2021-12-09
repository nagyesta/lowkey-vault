package com.github.nagyesta.lowkeyvault.service.secret;

import com.github.nagyesta.lowkeyvault.service.common.BaseVaultFake;
import com.github.nagyesta.lowkeyvault.service.secret.id.SecretEntityId;
import com.github.nagyesta.lowkeyvault.service.secret.id.VersionedSecretEntityId;

public interface SecretVaultFake extends BaseVaultFake<SecretEntityId, VersionedSecretEntityId, ReadOnlyKeyVaultSecretEntity> {

    VersionedSecretEntityId createSecretVersion(String secretName, String value, String contentType);
}
