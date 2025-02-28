package com.github.nagyesta.lowkeyvault.service.common.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.service.EntityId;
import com.github.nagyesta.lowkeyvault.service.common.BaseVaultEntity;
import com.github.nagyesta.lowkeyvault.service.common.VersionedEntityMultiMap;
import com.github.nagyesta.lowkeyvault.service.exception.NotFoundException;
import lombok.NonNull;
import org.springframework.util.Assert;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConcurrentVersionedEntityMultiMap<K extends EntityId, V extends K, RE extends BaseVaultEntity<V>, ME extends RE>
        implements VersionedEntityMultiMap<K, V, RE, ME> {

    private final BiFunction<String, String, V> versionCreateFunction;
    private final Map<String, Map<String, ME>> entities;
    private final Map<String, Deque<String>> versions;
    private final RecoveryLevel recoveryLevel;
    private final Integer recoverableDays;
    private final boolean deleted;

    public ConcurrentVersionedEntityMultiMap(@NonNull final RecoveryLevel recoveryLevel,
                                             final Integer recoverableDays,
                                             @NonNull final BiFunction<String, String, V> versionCreateFunction,
                                             final boolean deleted) {
        this.versionCreateFunction = versionCreateFunction;
        recoveryLevel.checkValidRecoverableDays(recoverableDays);
        this.recoveryLevel = recoveryLevel;
        this.recoverableDays = recoverableDays;
        this.deleted = deleted;
        entities = new ConcurrentHashMap<>();
        versions = new ConcurrentHashMap<>();
    }

    @Override
    public List<RE> listLatestEntities() {
        return streamAllLatestEntities()
                .collect(Collectors.toList());
    }

    @Override
    public List<RE> listLatestNonManagedEntities() {
        return streamAllLatestEntities()
                .filter(entity -> !entity.isManaged())
                .collect(Collectors.toList());
    }

    @Override
    public Deque<String> getVersions(@NonNull final K entityId) {
        if (!versions.containsKey(entityId.id())
                || versions.get(entityId.id()).isEmpty()) {
            throw new NotFoundException("Key not found: " + entityId);
        }
        return new LinkedList<>(versions.get(entityId.id()));
    }

    @Override
    public boolean containsName(@NonNull final String name) {
        return entities.containsKey(name);
    }

    @Override
    public boolean containsEntityMatching(final String name, final Predicate<RE> predicate) {
        return containsName(name) && entities.get(name).values().stream().anyMatch(predicate);
    }

    @Override
    public boolean containsEntity(@NonNull final K entityId) {
        return containsName(entityId.id()) && entities.get(entityId.id()).containsKey(entityId.version());
    }

    @Override
    public void assertContainsEntity(@NonNull final V entityId) {
        if (!containsEntity(entityId)) {
            throw new NotFoundException("Entity not found: " + entityId);
        }
    }

    @Override
    public V getLatestVersionOfEntity(@NonNull final K entityId) {
        final var availableVersions = getVersions(entityId);
        return versionCreateFunction.apply(entityId.id(), availableVersions.getLast());
    }

    @Override
    public RE getReadOnlyEntity(@NonNull final V entityId) {
        return getEntity(entityId);
    }

    @Override
    public ME getEntity(@NonNull final V entityId) {
        assertContainsEntity(entityId);
        return entities.get(entityId.id()).get(entityId.version());
    }

    @Override
    public void put(@NonNull final V entityId, @NonNull final ME entity) {
        entities.computeIfAbsent(entityId.id(), id -> new ConcurrentHashMap<>()).put(entityId.version(), entity);
        versions.computeIfAbsent(entityId.id(), id -> new ConcurrentLinkedDeque<>()).add(entityId.version());
    }

    @Override
    public <R extends RE> R getEntity(@NonNull final V entityId, @NonNull final Class<R> type) {
        return type.cast(this.getEntity(entityId));
    }

    @Override
    public RecoveryLevel getRecoveryLevel() {
        return recoveryLevel;
    }

    @Override
    public Optional<Integer> getRecoverableDays() {
        return Optional.ofNullable(recoverableDays);
    }

    @Override
    public boolean isDeleted() {
        return deleted;
    }

    @Override
    public void moveTo(@NonNull final K entityId,
                       @NonNull final VersionedEntityMultiMap<K, V, RE, ME> destination,
                       @NonNull final Function<ME, ME> applyToAll) {
        final var toKeep = entities.remove(entityId.id());
        final var versions = this.versions.remove(entityId.id());
        if (recoveryLevel.isRecoverable()) {
            versions.forEach(version -> destination
                    .put(versionCreateFunction.apply(entityId.id(), version), applyToAll.apply(toKeep.get(version))));
        }
    }

    @Override
    public void purgeExpired() {
        Assert.state(isDeleted(), "Purge cannot be called when map is not in deleted role.");
        final var purgeable = entities.entrySet().stream()
                .filter(e -> e.getValue().values().stream().anyMatch(ME::isPurgeExpired))
                .map(Map.Entry::getKey)
                .toList();
        purgeable.forEach(key -> {
            entities.remove(key);
            versions.remove(key);
        });
    }

    @Override
    public void purgeDeleted(@NonNull final K entityId) {
        Assert.state(isDeleted(), "Purge cannot be called when map is not in deleted role.");
        final var map = entities.get(entityId.id());
        Assert.state(map.values().stream().allMatch(ME::canPurge), "The selected elements cannot be purged.");
        entities.remove(entityId.id());
        versions.remove(entityId.id());
    }

    @Override
    public void forEachEntity(@NonNull final Consumer<ME> entityConsumer) {
        entities.values().forEach(entityVersions -> entityVersions.values().forEach(entityConsumer));
    }

    private Stream<ME> streamAllLatestEntities() {
        return entities.keySet().stream()
                .map(name -> versionCreateFunction.apply(name, versions.get(name).getLast()))
                .sorted(Comparator.comparing(EntityId::id))
                .map(this::getEntity);
    }
}
