package com.github.nagyesta.lowkeyvault.service.common.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.service.EntityId;
import com.github.nagyesta.lowkeyvault.service.common.BaseVaultEntity;
import com.github.nagyesta.lowkeyvault.service.common.BaseVaultFake;
import com.github.nagyesta.lowkeyvault.service.common.ReadOnlyVersionedEntityMultiMap;
import com.github.nagyesta.lowkeyvault.service.common.VersionedEntityMultiMap;
import com.github.nagyesta.lowkeyvault.service.exception.AlreadyExistsException;
import com.github.nagyesta.lowkeyvault.service.exception.NotFoundException;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import lombok.NonNull;
import org.springframework.util.Assert;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The base interface of the vault fakes.
 *
 * @param <K>  The type of the key (not versioned).
 * @param <V>  The versioned key type.
 * @param <RE> The read-only entity type.
 * @param <ME> The modifiable entity type.
 */
@SuppressWarnings("java:S119") //It is easier to ensure that the types are consistent this way
public abstract class BaseVaultFakeImpl<K extends EntityId, V extends K, RE extends BaseVaultEntity<V>, ME extends RE>
        implements BaseVaultFake<K, V, RE> {

    private final VaultFake vaultFake;
    private final VersionedEntityMultiMap<K, V, RE, ME> entities;
    private final VersionedEntityMultiMap<K, V, RE, ME> deletedEntities;

    protected BaseVaultFakeImpl(
            @NonNull final VaultFake vaultFake,
            @NonNull final RecoveryLevel recoveryLevel,
            final Integer recoverableDays) {
        recoveryLevel.checkValidRecoverableDays(recoverableDays);
        this.vaultFake = vaultFake;
        entities = new ConcurrentVersionedEntityMultiMap<>(
                recoveryLevel, recoverableDays, this::createVersionedId, false);
        deletedEntities = new ConcurrentVersionedEntityMultiMap<>(
                recoveryLevel, recoverableDays, this::createVersionedId, true);
    }

    @Override
    public ReadOnlyVersionedEntityMultiMap<K, V, RE> getEntities() {
        return entities;
    }

    @Override
    public ReadOnlyVersionedEntityMultiMap<K, V, RE> getDeletedEntities() {
        return deletedEntities;
    }

    @Override
    public void clearTags(@NonNull final V entityId) {
        entities.getEntity(entityId).setTags(new TreeMap<>());
    }

    @Override
    public void addTags(
            @NonNull final V entityId,
            final Map<String, String> tags) {
        final var newTags = new TreeMap<>(entities.getEntity(entityId).getTags());
        newTags.putAll(Objects.requireNonNullElse(tags, Collections.emptyMap()));
        entities.getEntity(entityId).setTags(newTags);
    }

    @Override
    public void setEnabled(
            @NonNull final V entityId,
            final boolean enabled) {
        entities.getEntity(entityId).setEnabled(enabled);
    }

    @Override
    public void setExpiry(
            @NonNull final V entityId,
            final OffsetDateTime notBefore,
            final OffsetDateTime expiry) {
        if (expiry != null && notBefore != null && notBefore.isAfter(expiry)) {
            throw new IllegalArgumentException("Expiry cannot be before notBefore.");
        }
        final var entity = entities.getEntity(entityId);
        entity.setNotBefore(notBefore);
        entity.setExpiry(expiry);
    }

    protected void setCreatedAndUpdatedOn(
            final V entityId,
            final OffsetDateTime created,
            final OffsetDateTime updated) {
        if (created == null && updated == null) {
            return;
        }
        final var createdOn = Optional.ofNullable(created).orElse(OffsetDateTime.now(ZoneOffset.UTC));
        final var updatedOn = Optional.ofNullable(updated).orElse(createdOn);
        if (createdOn.isAfter(updatedOn)) {
            throw new IllegalArgumentException("Updated cannot be before created.");
        }
        final var entity = entities.getEntity(entityId);
        entity.setCreatedOn(createdOn);
        entity.setUpdatedOn(updatedOn);
    }

    @Override
    public void delete(@NonNull final K entityId) {
        if (!entities.containsName(entityId.id())) {
            throw new NotFoundException("Entity not found: " + entityId);
        }
        entities.moveTo(entityId, deletedEntities, this::markDeleted);
    }

    @Override
    public void recover(@NonNull final K entityId) {
        deletedEntities.purgeExpired();
        if (!deletedEntities.containsName(entityId.id())) {
            throw new NotFoundException("Entity not found: " + entityId);
        }
        deletedEntities.moveTo(entityId, entities, this::markRestored);
    }

    @Override
    public void purge(@NonNull final K entityId) {
        deletedEntities.purgeExpired();
        if (!deletedEntities.containsName(entityId.id())) {
            throw new NotFoundException("Entity not found: " + entityId);
        }
        deletedEntities.purgeDeleted(entityId);
    }

    @Override
    public void timeShift(final int offsetSeconds) {
        Assert.isTrue(offsetSeconds > 0, "Offset must be positive.");
        this.entities.forEachEntity(entity -> entity.timeShift(offsetSeconds));
        this.deletedEntities.forEachEntity(entity -> entity.timeShift(offsetSeconds));
        this.deletedEntities.purgeExpired();
    }

    protected Set<String> keepNamesReadyForRemoval(final Set<String> names) {
        return names.stream()
                .filter(n -> !this.getEntities().containsName(n))
                .filter(n -> !this.getDeletedEntities().containsName(n))
                .collect(Collectors.toSet());
    }

    protected void setManaged(
            final V entityId,
            final boolean managed) {
        entities.getEntity(entityId).setManaged(managed);
    }

    protected abstract V createVersionedId(String id, String version);

    protected VersionedEntityMultiMap<K, V, RE, ME> getEntitiesInternal() {
        return entities;
    }

    protected VersionedEntityMultiMap<K, V, RE, ME> getDeletedEntitiesInternal() {
        return deletedEntities;
    }

    protected VaultFake vaultFake() {
        return vaultFake;
    }

    protected V addVersion(
            @org.springframework.lang.NonNull final V entityId,
            @org.springframework.lang.NonNull final ME entity) {
        assertNoConflict(entityId);
        entities.put(entityId, entity);
        return entityId;
    }

    private void assertNoConflict(final V entityId) {
        deletedEntities.purgeExpired();
        if (!entities.containsName(entityId.id()) && deletedEntities.containsName(entityId.id())) {
            throw new AlreadyExistsException("A deleted entity already exists with this name: " + entityId);
        }
    }

    private ME markDeleted(final ME entity) {
        final int days = entity.getRecoverableDays();
        final var now = OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
        entity.setDeletedDate(now);
        entity.setScheduledPurgeDate(now.plusDays(days));
        return entity;
    }

    private ME markRestored(final ME entity) {
        entity.setDeletedDate(null);
        entity.setScheduledPurgeDate(null);
        return entity;
    }
}
