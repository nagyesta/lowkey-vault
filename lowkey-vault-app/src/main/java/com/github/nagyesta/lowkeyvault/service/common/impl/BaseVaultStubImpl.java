package com.github.nagyesta.lowkeyvault.service.common.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.service.EntityId;
import com.github.nagyesta.lowkeyvault.service.common.BaseVaultEntity;
import com.github.nagyesta.lowkeyvault.service.common.BaseVaultStub;
import com.github.nagyesta.lowkeyvault.service.exception.NotFoundException;
import com.github.nagyesta.lowkeyvault.service.vault.VaultStub;
import lombok.NonNull;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public abstract class BaseVaultStubImpl<K extends EntityId, V extends K, E extends BaseVaultEntity, M extends E>
        implements BaseVaultStub<K, V, E> {

    private final VaultStub vaultStub;
    private final Map<String, Map<String, M>> entities;
    private final Map<String, Deque<String>> versions;

    public BaseVaultStubImpl(@NonNull final VaultStub vaultStub) {
        this.vaultStub = vaultStub;
        entities = new ConcurrentHashMap<>();
        versions = new ConcurrentHashMap<>();
    }

    @Override
    public Deque<String> getVersions(@NonNull final K entityId) {
        if (!vaultStub.matches(entityId.vault())
                || !versions.containsKey(entityId.id())
                || versions.get(entityId.id()).isEmpty()) {
            throw new NotFoundException("Key not found: " + entityId);
        }
        return new LinkedList<>(versions.get(entityId.id()));
    }

    @Override
    public V getLatestVersionOfEntity(@NonNull final K entityId) {
        final Deque<String> availableVersions = getVersions(entityId);
        return createVersionedId(entityId.id(), availableVersions.getLast());
    }

    protected abstract V createVersionedId(String id, String version);

    @Override
    public void clearTags(@NonNull final V entityId) {
        assertHasEntity(entityId);
        doGetEntity(entityId).setTags(new TreeMap<>());
    }

    @Override
    public void addTags(@NonNull final V entityId, final Map<String, String> tags) {
        assertHasEntity(entityId);
        final TreeMap<String, String> newTags = new TreeMap<>(getEntity(entityId).getTags());
        newTags.putAll(Objects.requireNonNullElse(tags, Collections.emptyMap()));
        doGetEntity(entityId).setTags(newTags);
    }

    @Override
    public void setEnabled(@NonNull final V entityId, final boolean enabled) {
        assertHasEntity(entityId);
        doGetEntity(entityId).setEnabled(enabled);
    }

    @Override
    public void setExpiry(@NonNull final V entityId,
                          final OffsetDateTime notBefore,
                          final OffsetDateTime expiry) {
        assertHasEntity(entityId);
        if (expiry != null && notBefore != null && notBefore.isAfter(expiry)) {
            throw new IllegalArgumentException("Expiry cannot be before notBefore.");
        }
        final M entity = doGetEntity(entityId);
        entity.setNotBefore(notBefore);
        entity.setExpiry(expiry);
    }

    @Override
    public void setRecovery(@NonNull final V entityId,
                            @NonNull final RecoveryLevel recoveryLevel,
                            final Integer recoverableDays) {
        assertHasEntity(entityId);
        recoveryLevel.checkValidRecoverableDays(recoverableDays);
        final M entity = doGetEntity(entityId);
        entity.setRecoveryLevel(recoveryLevel);
        entity.setRecoverableDays(recoverableDays);
    }

    @Override
    public <R extends E> R getEntity(
            @NonNull final V entityId, @NonNull final Class<R> type) {
        return type.cast(getEntity(entityId));
    }

    @Override
    public E getEntity(@NonNull final V entityId) {
        return doGetEntity(entityId);
    }

    protected M doGetEntity(@org.springframework.lang.NonNull final V entityId) {
        return entities.get(entityId.id()).get(entityId.version());
    }

    protected VaultStub vaultStub() {
        return vaultStub;
    }

    protected V addVersion(@org.springframework.lang.NonNull final V entityId,
                           @org.springframework.lang.NonNull final M entity) {
        entities.computeIfAbsent(entityId.id(), id -> new ConcurrentHashMap<>()).put(entityId.version(), entity);
        versions.computeIfAbsent(entityId.id(), id -> new ConcurrentLinkedDeque<>()).add(entityId.version());
        return entityId;
    }

    protected void assertHasEntity(
            @org.springframework.lang.NonNull final V entityId) {
        if (!entities.containsKey(entityId.id())
                || !entities.get(entityId.id()).containsKey(entityId.version())) {
            throw new NotFoundException("Entity not found: " + entityId);
        }
    }
}
