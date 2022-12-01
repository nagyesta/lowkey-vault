package com.github.nagyesta.lowkeyvault.service.secret;

import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertContentType;
import com.github.nagyesta.lowkeyvault.service.common.BaseVaultFake;
import com.github.nagyesta.lowkeyvault.service.secret.id.SecretEntityId;
import com.github.nagyesta.lowkeyvault.service.secret.id.VersionedSecretEntityId;

import java.time.OffsetDateTime;

public interface SecretVaultFake extends BaseVaultFake<SecretEntityId, VersionedSecretEntityId, ReadOnlyKeyVaultSecretEntity> {

    VersionedSecretEntityId createSecretVersion(String secretName, String value, String contentType);

    VersionedSecretEntityId createSecretVersion(VersionedSecretEntityId entityId, String value, String contentType);

    VersionedSecretEntityId createSecretVersionForCertificate(
            VersionedSecretEntityId id, String value, CertContentType contentType, OffsetDateTime notBefore, OffsetDateTime expiry);
}
