package com.github.nagyesta.lowkeyvault.service.key.impl;

import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.AesJsonWebKeyImportRequestConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.EcJsonWebKeyImportRequestConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.RsaJsonWebKeyImportRequestConverter;
import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyCurveName;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyOperation;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.JsonWebKeyImportRequest;
import com.github.nagyesta.lowkeyvault.service.common.impl.BaseVaultFakeImpl;
import com.github.nagyesta.lowkeyvault.service.key.KeyVaultFake;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyRotationPolicy;
import com.github.nagyesta.lowkeyvault.service.key.RotationPolicy;
import com.github.nagyesta.lowkeyvault.service.key.id.KeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.util.PeriodUtil;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import lombok.NonNull;
import org.springframework.util.Assert;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class KeyVaultFakeImpl
        extends BaseVaultFakeImpl<KeyEntityId, VersionedKeyEntityId, ReadOnlyKeyVaultKeyEntity, KeyVaultKeyEntity<?, ?>>
        implements KeyVaultFake {

    private final RsaJsonWebKeyImportRequestConverter rsaConverter = new RsaJsonWebKeyImportRequestConverter();
    private final EcJsonWebKeyImportRequestConverter ecConverter = new EcJsonWebKeyImportRequestConverter();
    private final AesJsonWebKeyImportRequestConverter aesConverter = new AesJsonWebKeyImportRequestConverter();

    private final ConcurrentMap<String, RotationPolicy> rotationPolicies = new ConcurrentHashMap<>();

    public KeyVaultFakeImpl(@org.springframework.lang.NonNull final VaultFake vaultFake,
                            @org.springframework.lang.NonNull final RecoveryLevel recoveryLevel,
                            final Integer recoverableDays) {
        super(vaultFake, recoveryLevel, recoverableDays);
    }

    @Override
    protected VersionedKeyEntityId createVersionedId(final String id, final String version) {
        return new VersionedKeyEntityId(vaultFake().baseUri(), id, version);
    }

    @Override
    public <E, T extends KeyCreationInput<E>> VersionedKeyEntityId createKeyVersion(
            @NonNull final String keyName, @NonNull final T input) {
        return input.getKeyType().createKey(this, keyName, input);
    }

    @Override
    public <E, T extends KeyCreationInput<E>> VersionedKeyEntityId createKeyVersionForCertificate(
            @NonNull final String keyName,
            @NonNull final T input,
            @NonNull final OffsetDateTime notBefore,
            @NonNull final OffsetDateTime expiry) {
        final VersionedKeyEntityId entityId = this.createKeyVersion(keyName, input);
        this.setKeyOperations(entityId, List.of(
                KeyOperation.SIGN, KeyOperation.VERIFY,
                KeyOperation.ENCRYPT, KeyOperation.DECRYPT,
                KeyOperation.WRAP_KEY, KeyOperation.UNWRAP_KEY));
        this.setEnabled(entityId, true);
        this.setExpiry(entityId, notBefore, expiry);
        this.setManaged(entityId, true);
        return entityId;
    }

    @Override
    public VersionedKeyEntityId importKeyVersion(
            final String keyName, final JsonWebKeyImportRequest key) {
        final VersionedKeyEntityId keyEntityId = new VersionedKeyEntityId(vaultFake().baseUri(), keyName);
        return importKeyVersion(keyEntityId, key);
    }

    @Override
    public VersionedKeyEntityId importManagedKeyVersion(final String keyName, final JsonWebKeyImportRequest key) {
        final VersionedKeyEntityId keyEntityId = importKeyVersion(keyName, key);
        this.setManaged(keyEntityId, true);
        return keyEntityId;
    }

    @Override
    public VersionedKeyEntityId importKeyVersion(
            final VersionedKeyEntityId keyEntityId, final JsonWebKeyImportRequest key) {
        final KeyType keyType = Objects.requireNonNull(key).getKeyType();
        return keyType.importKey(this, keyEntityId, key);
    }

    @Override
    public VersionedKeyEntityId importRsaKeyVersion(
            final VersionedKeyEntityId keyEntityId, final JsonWebKeyImportRequest key) {
        final KeyType keyType = Objects.requireNonNull(key).getKeyType();
        Assert.isTrue(keyType.isRsa(), "RSA key expected, but found: " + keyType.name());
        final RsaKeyVaultKeyEntity keyEntity = new RsaKeyVaultKeyEntity(keyEntityId, vaultFake(), rsaConverter.convert(key),
                rsaConverter.getKeyParameter(key), keyType.isHsm());
        setExpiryBasedOnRotationPolicy(keyEntityId, keyEntity);
        return addVersion(keyEntityId, keyEntity);
    }

    @Override
    public VersionedKeyEntityId importEcKeyVersion(
            final VersionedKeyEntityId keyEntityId, final JsonWebKeyImportRequest key) {
        final KeyType keyType = Objects.requireNonNull(key).getKeyType();
        Assert.isTrue(keyType.isEc(), "EC key expected, but found: " + keyType.name());
        final EcKeyVaultKeyEntity keyEntity = new EcKeyVaultKeyEntity(keyEntityId, vaultFake(), ecConverter.convert(key),
                ecConverter.getKeyParameter(key), keyType.isHsm());
        setExpiryBasedOnRotationPolicy(keyEntityId, keyEntity);
        return addVersion(keyEntityId, keyEntity);
    }

    @Override
    public VersionedKeyEntityId importOctKeyVersion(
            final VersionedKeyEntityId keyEntityId, final JsonWebKeyImportRequest key) {
        final KeyType keyType = Objects.requireNonNull(key).getKeyType();
        Assert.isTrue(keyType.isOct(), "OCT key expected, but found: " + keyType.name());
        Assert.isTrue(keyType.isHsm(), "OCT keys are only supported using HSM.");
        final AesKeyVaultKeyEntity keyEntity = new AesKeyVaultKeyEntity(keyEntityId, vaultFake(), aesConverter.convert(key),
                aesConverter.getKeyParameter(key), keyType.isHsm());
        setExpiryBasedOnRotationPolicy(keyEntityId, keyEntity);
        return addVersion(keyEntityId, keyEntity);
    }

    @Override
    public VersionedKeyEntityId createRsaKeyVersion(
            @NonNull final String keyName, @NonNull final RsaKeyCreationInput input) {
        final VersionedKeyEntityId keyEntityId = new VersionedKeyEntityId(vaultFake().baseUri(), keyName);
        final RsaKeyVaultKeyEntity keyEntity = new RsaKeyVaultKeyEntity(keyEntityId, vaultFake(),
                input.getKeyParameter(), input.getPublicExponent(), input.getKeyType().isHsm());
        setExpiryBasedOnRotationPolicy(keyEntityId, keyEntity);
        return addVersion(keyEntityId, keyEntity);
    }

    @Override
    public VersionedKeyEntityId createEcKeyVersion(
            @NonNull final String keyName, @NonNull final EcKeyCreationInput input) {
        final VersionedKeyEntityId keyEntityId = new VersionedKeyEntityId(vaultFake().baseUri(), keyName);
        input.getKeyType().validate(input.getKeyParameter(), KeyCurveName.class);
        final EcKeyVaultKeyEntity keyEntity = new EcKeyVaultKeyEntity(keyEntityId, vaultFake(),
                input.getKeyParameter(), input.getKeyType().isHsm());
        setExpiryBasedOnRotationPolicy(keyEntityId, keyEntity);
        return addVersion(keyEntityId, keyEntity);
    }

    @Override
    public VersionedKeyEntityId createOctKeyVersion(
            @NonNull final String keyName, @NonNull final OctKeyCreationInput input) {
        final VersionedKeyEntityId keyEntityId = new VersionedKeyEntityId(vaultFake().baseUri(), keyName);
        Assert.isTrue(input.getKeyType().isHsm(), "OCT keys are only supported using HSM.");
        final AesKeyVaultKeyEntity keyEntity = new AesKeyVaultKeyEntity(keyEntityId, vaultFake(),
                input.getKeyParameter(), input.getKeyType().isHsm());
        setExpiryBasedOnRotationPolicy(keyEntityId, keyEntity);
        return addVersion(keyEntityId, keyEntity);
    }

    @Override
    public void setKeyOperations(@NonNull final VersionedKeyEntityId keyEntityId,
                                 final List<KeyOperation> keyOperations) {
        getEntitiesInternal().getEntity(keyEntityId).setOperations(Objects.requireNonNullElse(keyOperations, Collections.emptyList()));
    }

    @Override
    public void timeShift(final int offsetSeconds) {
        super.timeShift(offsetSeconds);
        rotationPolicies.values().forEach(p -> p.timeShift(offsetSeconds));
        performPastRotations();
    }

    private void performPastRotations() {
        rotationPolicies.values().stream()
                .filter(ReadOnlyRotationPolicy::isAutoRotate)
                .forEach(this::performMissedRotationsOfPolicy);
    }

    @Override
    public RotationPolicy rotationPolicy(@NonNull final KeyEntityId keyEntityId) {
        purgeDeletedPolicies();
        return rotationPolicies.get(keyEntityId.id());
    }

    @Override
    public void setRotationPolicy(@NonNull final RotationPolicy rotationPolicy) {
        final ReadOnlyKeyVaultKeyEntity readOnlyEntity = latestReadOnlyKeyVersion(rotationPolicy.getId());
        rotationPolicy.validate(readOnlyEntity.getExpiry().orElse(null));
        final RotationPolicy existingPolicy = rotationPolicy(rotationPolicy.getId());
        if (existingPolicy == null) {
            rotationPolicies.put(rotationPolicy.getId().id(), rotationPolicy);
        } else {
            existingPolicy.setLifetimeActions(rotationPolicy.getLifetimeActions());
            existingPolicy.setExpiryTime(rotationPolicy.getExpiryTime());
        }
    }

    @Override
    public VersionedKeyEntityId rotateKey(@NonNull final KeyEntityId keyEntityId) {
        final ReadOnlyKeyVaultKeyEntity readOnlyEntity = latestReadOnlyKeyVersion(keyEntityId);
        final VersionedKeyEntityId rotatedKeyId = createKeyVersion(keyEntityId.id(), readOnlyEntity.keyCreationInput());
        final KeyVaultKeyEntity<?, ?> rotatedEntity = getEntities().getEntity(rotatedKeyId, KeyVaultKeyEntity.class);
        rotatedEntity.setOperations(readOnlyEntity.getOperations());
        rotatedEntity.setEnabled(true);
        rotatedEntity.setTags(readOnlyEntity.getTags());
        return rotatedKeyId;
    }

    private void setExpiryBasedOnRotationPolicy(final VersionedKeyEntityId keyEntityId, final KeyVaultKeyEntity<?, ?> keyEntity) {
        final Optional<Long> expiryDays = Optional.ofNullable(rotationPolicies)
                .map(policies -> policies.get(keyEntityId.id()))
                .map(ReadOnlyRotationPolicy::getExpiryTime)
                .map(PeriodUtil::asDays);
        expiryDays.ifPresent(days -> keyEntity.setExpiry(keyEntity.getCreated().plusDays(days)));
    }

    private ReadOnlyKeyVaultKeyEntity latestReadOnlyKeyVersion(final KeyEntityId keyEntityId) {
        final VersionedKeyEntityId latestVersionOfEntity = getEntities().getLatestVersionOfEntity(keyEntityId);
        return getEntities().getReadOnlyEntity(latestVersionOfEntity);
    }

    private void performMissedRotationsOfPolicy(final RotationPolicy rotationPolicy) {
        final KeyEntityId keyEntityId = rotationPolicy.getId();
        final VersionedKeyEntityId latestVersionOfEntity = getEntities().getLatestVersionOfEntity(keyEntityId);
        final ReadOnlyKeyVaultKeyEntity readOnlyEntity = getEntities().getReadOnlyEntity(latestVersionOfEntity);
        rotationPolicy.missedRotations(readOnlyEntity.getCreated())
                .forEach(rotationTime -> simulatePointInTimeRotation(keyEntityId, rotationTime));
    }

    private void simulatePointInTimeRotation(final KeyEntityId keyEntityId, final OffsetDateTime rotationTime) {
        final OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        final int diffSeconds = (int) (now.toEpochSecond() - rotationTime.toEpochSecond());
        final VersionedKeyEntityId versionedKeyEntityId = rotateKey(keyEntityId);
        getEntities().getEntity(versionedKeyEntityId, KeyVaultKeyEntity.class).timeShift(diffSeconds);
    }

    private void purgeDeletedPolicies() {
        keepNamesReadyForRemoval(rotationPolicies.keySet())
                .forEach(rotationPolicies::remove);
    }
}
