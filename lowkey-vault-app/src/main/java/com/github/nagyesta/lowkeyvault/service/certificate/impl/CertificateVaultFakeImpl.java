package com.github.nagyesta.lowkeyvault.service.certificate.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.CertificateRestoreInput;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateVaultFake;
import com.github.nagyesta.lowkeyvault.service.certificate.LifetimeActionPolicy;
import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import com.github.nagyesta.lowkeyvault.service.certificate.id.CertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.certificate.id.VersionedCertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.common.impl.BaseVaultFakeImpl;
import com.github.nagyesta.lowkeyvault.service.key.id.KeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.impl.KeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.secret.id.SecretEntityId;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import lombok.NonNull;

import java.time.OffsetDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

public class CertificateVaultFakeImpl
        extends BaseVaultFakeImpl<CertificateEntityId, VersionedCertificateEntityId,
        ReadOnlyKeyVaultCertificateEntity, KeyVaultCertificateEntity>
        implements CertificateVaultFake {

    private final ConcurrentMap<String, LifetimeActionPolicy> lifetimeActionPolicies = new ConcurrentHashMap<>();

    public CertificateVaultFakeImpl(@org.springframework.lang.NonNull final VaultFake vaultFake,
                                    @org.springframework.lang.NonNull final RecoveryLevel recoveryLevel,
                                    final Integer recoverableDays) {
        super(vaultFake, recoveryLevel, recoverableDays);
    }

    @Override
    protected VersionedCertificateEntityId createVersionedId(final String id, final String version) {
        return new VersionedCertificateEntityId(vaultFake().baseUri(), id, version);
    }

    @Override
    public VersionedCertificateEntityId createCertificateVersion(
            @NonNull final String name, @NonNull final CertificateCreationInput input) {
        final var entity = new KeyVaultCertificateEntity(name, input, vaultFake());
        return addVersion(entity.getId(), entity);
    }

    @Override
    public VersionedCertificateEntityId importCertificateVersion(
            @NonNull final String name, @NonNull final CertificateImportInput input) {
        final var entity = new KeyVaultCertificateEntity(
                name, input, vaultFake());
        return addVersion(entity.getId(), entity);
    }

    @Override
    public void restoreCertificateVersion(
            @NonNull final VersionedCertificateEntityId versionedEntityId, @NonNull final CertificateRestoreInput input) {
        final var entity = new KeyVaultCertificateEntity(versionedEntityId, input, vaultFake());
        addVersion(entity.getId(), entity);
    }

    @Override
    public void timeShift(final int offsetSeconds) {
        super.timeShift(offsetSeconds);
        lifetimeActionPolicies.values().forEach(p -> p.timeShift(offsetSeconds));
        performPastRenewals();
    }

    private void performPastRenewals() {
        purgeDeletedPolicies();
        lifetimeActionPolicies.values().stream()
                .filter(LifetimeActionPolicy::isAutoRenew)
                .filter(l -> getEntities().containsName(l.getId().id()))
                .forEach(this::performMissedRenewalsOfPolicy);
    }

    private void performMissedRenewalsOfPolicy(final LifetimeActionPolicy lifetimeActionPolicy) {
        final var certificateEntityId = lifetimeActionPolicy.getId();
        final var latestVersionOfEntity = getEntities().getLatestVersionOfEntity(certificateEntityId);
        final var readOnlyEntity = getEntities().getReadOnlyEntity(latestVersionOfEntity);
        final Function<OffsetDateTime, OffsetDateTime> createdToExpiryFunction = s -> s
                .plusMonths(readOnlyEntity.getIssuancePolicy().getValidityMonths());
        lifetimeActionPolicy.missedRenewalDays(readOnlyEntity.getCreated(), createdToExpiryFunction)
                .forEach(renewalTime -> simulatePointInTimeRotation(certificateEntityId, renewalTime));
    }

    private void simulatePointInTimeRotation(final CertificateEntityId certificateEntityId, final OffsetDateTime renewalTime) {
        final var latest = latestReadOnlyCertificateVersion(certificateEntityId);
        final var input = new CertificatePolicy(latest.getIssuancePolicy());
        input.setValidityStart(renewalTime);
        final var kid = rotateIfNeededAndGetLastKeyId(input);
        final var id = generateIdOfNewCertificateEntity(input, kid);
        final var entity = new KeyVaultCertificateEntity(input, kid, id, vaultFake());
        addVersion(entity.getId(), entity);
    }

    private VersionedCertificateEntityId generateIdOfNewCertificateEntity(
            final ReadOnlyCertificatePolicy input, final VersionedKeyEntityId kid) {
        final VersionedCertificateEntityId id;
        if (input.isReuseKeyOnRenewal()) {
            id = new VersionedCertificateEntityId(vaultFake().baseUri(), input.getName());
        } else {
            id = new VersionedCertificateEntityId(vaultFake().baseUri(), input.getName(), kid.version());
        }
        return id;
    }

    private VersionedKeyEntityId rotateIfNeededAndGetLastKeyId(final ReadOnlyCertificatePolicy input) {
        final var keyVaultFake = vaultFake().keyVaultFake();
        final var entities = keyVaultFake
                .getEntities();
        final VersionedKeyEntityId versionedKeyEntityId;
        if (input.isReuseKeyOnRenewal()) {
            final var lastVersion = entities.getVersions(new KeyEntityId(vaultFake().baseUri(), input.getName())).getLast();
            versionedKeyEntityId = new VersionedKeyEntityId(vaultFake().baseUri(), input.getName(), lastVersion);
            final var notBefore = entities.getReadOnlyEntity(versionedKeyEntityId)
                    .getNotBefore().orElseThrow(() -> new IllegalStateException("Managed keys should always have notBefore timestamps."));
            final var newExpiry = input.getValidityStart().plusMonths(input.getValidityMonths());
            //extend expiry until the certificate expiry
            keyVaultFake.setExpiry(versionedKeyEntityId, notBefore, newExpiry);
        } else {
            versionedKeyEntityId = keyVaultFake.rotateKey(new KeyEntityId(vaultFake().baseUri(), input.getName()));
            //update timestamps
            final var notBefore = input.getValidityStart();
            final var expiry = notBefore.plusMonths(input.getValidityMonths());
            keyVaultFake.setExpiry(versionedKeyEntityId, notBefore, expiry);
            final KeyVaultKeyEntity<?, ?> entity = keyVaultFake.getEntities().getEntity(versionedKeyEntityId, KeyVaultKeyEntity.class);
            entity.setManaged(true);
            entity.setCreatedOn(notBefore);
            entity.setUpdatedOn(notBefore);
        }
        return versionedKeyEntityId;
    }

    @Override
    public void delete(@NonNull final CertificateEntityId entityId) {
        super.delete(entityId);
        vaultFake().keyVaultFake().delete(toKeyEntityId(entityId));
        vaultFake().secretVaultFake().delete(toSecretEntityId(entityId));
    }

    @Override
    public void recover(@NonNull final CertificateEntityId entityId) {
        super.recover(entityId);
        vaultFake().keyVaultFake().recover(toKeyEntityId(entityId));
        vaultFake().secretVaultFake().recover(toSecretEntityId(entityId));
    }

    @Override
    public void purge(@NonNull final CertificateEntityId entityId) {
        super.purge(entityId);
        vaultFake().keyVaultFake().purge(toKeyEntityId(entityId));
        vaultFake().secretVaultFake().purge(toSecretEntityId(entityId));
    }

    @Override
    public LifetimeActionPolicy lifetimeActionPolicy(@NonNull final CertificateEntityId certificateEntityId) {
        purgeDeletedPolicies();
        return lifetimeActionPolicies.get(certificateEntityId.id());
    }

    @Override
    public void setLifetimeActionPolicy(@NonNull final LifetimeActionPolicy lifetimeActionPolicy) {
        final var readOnlyEntity = latestReadOnlyCertificateVersion(lifetimeActionPolicy.getId());
        lifetimeActionPolicy.validate(readOnlyEntity.getIssuancePolicy().getValidityMonths());
        final var existingPolicy = lifetimeActionPolicy(lifetimeActionPolicy.getId());
        if (existingPolicy == null) {
            lifetimeActionPolicies.put(lifetimeActionPolicy.getId().id(), lifetimeActionPolicy);
        } else {
            existingPolicy.setLifetimeActions(lifetimeActionPolicy.getLifetimeActions());
        }
    }

    @Override
    public void regenerateCertificates() {
        this.getEntitiesInternal().forEachEntity(entity -> entity.regenerateCertificate(this.vaultFake()));
        this.getDeletedEntitiesInternal().forEachEntity(entity -> entity.regenerateCertificate(this.vaultFake()));
    }

    private void purgeDeletedPolicies() {
        keepNamesReadyForRemoval(lifetimeActionPolicies.keySet())
                .forEach(lifetimeActionPolicies::remove);
    }

    private ReadOnlyKeyVaultCertificateEntity latestReadOnlyCertificateVersion(final CertificateEntityId certificateEntityId) {
        final var latestVersionOfEntity = getEntities().getLatestVersionOfEntity(certificateEntityId);
        return getEntities().getReadOnlyEntity(latestVersionOfEntity);
    }

    private KeyEntityId toKeyEntityId(final CertificateEntityId entityId) {
        return new KeyEntityId(entityId.vault(), entityId.id());
    }

    private SecretEntityId toSecretEntityId(final CertificateEntityId entityId) {
        return new SecretEntityId(entityId.vault(), entityId.id());
    }
}
