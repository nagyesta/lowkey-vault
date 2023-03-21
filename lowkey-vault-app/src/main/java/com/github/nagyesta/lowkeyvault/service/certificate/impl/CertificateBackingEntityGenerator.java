package com.github.nagyesta.lowkeyvault.service.certificate.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyOperation;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.JsonWebKeyImportRequest;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyAsymmetricKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.impl.KeyCreateDetailedInput;
import com.github.nagyesta.lowkeyvault.service.key.impl.KeyImportInput;
import com.github.nagyesta.lowkeyvault.service.secret.id.VersionedSecretEntityId;
import com.github.nagyesta.lowkeyvault.service.secret.impl.KeyVaultSecretEntity;
import com.github.nagyesta.lowkeyvault.service.secret.impl.SecretCreateInput;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;

import java.security.KeyPair;
import java.security.cert.Certificate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

public class CertificateBackingEntityGenerator {
    private final VaultFake vaultFake;

    public CertificateBackingEntityGenerator(final VaultFake vaultFake) {
        this.vaultFake = vaultFake;
    }

    public VersionedKeyEntityId generateKeyPair(final ReadOnlyCertificatePolicy input) {
        final OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        final OffsetDateTime expiry = now.plusMonths(input.getValidityMonths());
        return vaultFake.keyVaultFake().createKeyVersion(input.getName(), KeyCreateDetailedInput.builder()
                .key(input.toKeyCreationInput())
                .keyOperations(List.of(
                        KeyOperation.SIGN, KeyOperation.VERIFY,
                        KeyOperation.ENCRYPT, KeyOperation.DECRYPT,
                        KeyOperation.WRAP_KEY, KeyOperation.UNWRAP_KEY))
                .notBefore(now)
                .expiresOn(expiry)
                .enabled(true)
                .managed(true)
                .build());
    }

    public VersionedKeyEntityId importKeyPair(
            final ReadOnlyCertificatePolicy input, final JsonWebKeyImportRequest keyImportRequest) {
        return vaultFake.keyVaultFake().importKeyVersion(input.getName(), KeyImportInput.builder()
                .key(keyImportRequest)
                .createdOn(input.getValidityStart())
                .updatedOn(input.getValidityStart())
                .expiresOn(input.getValidityStart().plusMonths(input.getValidityMonths()))
                .notBefore(input.getValidityStart())
                .managed(true)
                .enabled(true)
                .build());
    }

    public VersionedSecretEntityId generateSecret(final ReadOnlyCertificatePolicy input,
                                                  final Certificate certificate,
                                                  final VersionedKeyEntityId kid,
                                                  final VersionedSecretEntityId sid) {
        final KeyPair key = vaultFake.keyVaultFake().getEntities().getEntity(kid, ReadOnlyAsymmetricKeyVaultKeyEntity.class).getKey();
        final String value = input.getContentType().asBase64CertificatePackage(certificate, key);
        final OffsetDateTime start = input.getValidityStart();
        final OffsetDateTime expiry = start.plusMonths(input.getValidityMonths());
        return vaultFake.secretVaultFake().createSecretVersion(sid, SecretCreateInput.builder()
                .value(value)
                .contentType(input.getContentType().getMimeType())
                .createdOn(start)
                .updatedOn(start)
                .notBefore(start)
                .expiresOn(expiry)
                .enabled(true)
                .managed(true)
                .build());
    }

    public void updateSecretValueWithNewCertificate(final CertificatePolicy updated,
                                                    final Certificate certificate,
                                                    final VersionedKeyEntityId kid,
                                                    final VersionedSecretEntityId sid) {
        final ReadOnlyAsymmetricKeyVaultKeyEntity key = vaultFake.keyVaultFake().getEntities()
                .getEntity(kid, ReadOnlyAsymmetricKeyVaultKeyEntity.class);
        final KeyVaultSecretEntity secret = vaultFake.secretVaultFake().getEntities().getEntity(sid, KeyVaultSecretEntity.class);
        secret.setValue(updated.getContentType().asBase64CertificatePackage(certificate, key.getKey()));
    }
}
