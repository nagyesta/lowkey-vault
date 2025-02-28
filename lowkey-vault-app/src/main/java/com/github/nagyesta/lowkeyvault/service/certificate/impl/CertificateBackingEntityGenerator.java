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
import org.springframework.util.Assert;

import java.security.cert.Certificate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

public class CertificateBackingEntityGenerator {
    private final VaultFake vaultFake;

    public CertificateBackingEntityGenerator(final VaultFake vaultFake) {
        this.vaultFake = vaultFake;
    }

    public VersionedKeyEntityId generateKeyPair(final ReadOnlyCertificatePolicy input) {
        final var now = OffsetDateTime.now(ZoneOffset.UTC);
        final var expiry = now.plusMonths(input.getValidityMonths());
        return vaultFake.keyVaultFake().createKeyVersion(input.getName(), KeyCreateDetailedInput.builder()
                .key(input.toKeyCreationInput())
                .keyOperations(determineKeyOperations(input))
                .notBefore(now)
                .expiresOn(expiry)
                .enabled(true)
                .managed(true)
                .build());
    }

    public VersionedKeyEntityId importKeyPair(
            final ReadOnlyCertificatePolicy input, final JsonWebKeyImportRequest keyImportRequest) {
        return importKeyPair(new VersionedKeyEntityId(vaultFake.baseUri(), input.getName()), input, keyImportRequest, true);
    }

    public VersionedKeyEntityId importKeyPair(
            final VersionedKeyEntityId kid,
            final ReadOnlyCertificatePolicy input,
            final JsonWebKeyImportRequest keyImportRequest,
            final boolean enabled) {
        Assert.isTrue(kid.id().equals(input.getName()), "The key id must match the policy name.");
        keyImportRequest.setKeyOps(determineKeyOperations(input));
        return vaultFake.keyVaultFake().importKeyVersion(kid, KeyImportInput.builder()
                .key(keyImportRequest)
                .createdOn(input.getValidityStart())
                .updatedOn(input.getValidityStart())
                .expiresOn(input.getValidityStart().plusMonths(input.getValidityMonths()))
                .notBefore(input.getValidityStart())
                .managed(true)
                .enabled(enabled)
                .build());
    }

    public VersionedSecretEntityId generateSecret(
            final ReadOnlyCertificatePolicy input,
            final Certificate certificate,
            final VersionedKeyEntityId kid,
            final VersionedSecretEntityId sid) {
        final var key = vaultFake.keyVaultFake().getEntities()
                .getEntity(kid, ReadOnlyAsymmetricKeyVaultKeyEntity.class).getKey();
        final var value = input.getContentType().asBase64CertificatePackage(certificate, key);
        final var start = input.getValidityStart();
        final var expiry = start.plusMonths(input.getValidityMonths());
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

    public void updateSecretValueWithNewCertificate(
            final CertificatePolicy updated,
            final Certificate certificate,
            final VersionedKeyEntityId kid,
            final VersionedSecretEntityId sid) {
        final var key = vaultFake.keyVaultFake().getEntities()
                .getEntity(kid, ReadOnlyAsymmetricKeyVaultKeyEntity.class);
        final var secret = vaultFake.secretVaultFake().getEntities()
                .getEntity(sid, KeyVaultSecretEntity.class);
        secret.setValue(updated.getContentType().asBase64CertificatePackage(certificate, key.getKey()));
    }

    private static List<KeyOperation> determineKeyOperations(final ReadOnlyCertificatePolicy input) {
        return input.getKeyUsage().stream()
                .map(KeyUsageEnum::getKeyOperations)
                .flatMap(Set::stream)
                .distinct()
                .sorted()
                .toList();
    }
}
