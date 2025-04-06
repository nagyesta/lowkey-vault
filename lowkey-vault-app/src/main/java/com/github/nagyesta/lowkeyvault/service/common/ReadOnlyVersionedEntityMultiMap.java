package com.github.nagyesta.lowkeyvault.service.common;

import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.service.EntityId;

import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@SuppressWarnings("java:S119") //It is easier to ensure that the types are consistent this way
public interface ReadOnlyVersionedEntityMultiMap<K extends EntityId, V extends K, RE extends BaseVaultEntity<V>> {

    List<RE> listLatestEntities();

    List<RE> listLatestNonManagedEntities();

    Deque<String> getVersions(K entityId);

    boolean containsName(String name);

    boolean containsEntityMatching(String name, Predicate<RE> predicate);

    boolean containsEntity(K entityId);

    void assertContainsEntity(V entityId);

    V getLatestVersionOfEntity(K entityId);

    <R extends RE> R getEntity(V entityId, Class<R> type);

    RE getReadOnlyEntity(V entityId);

    RecoveryLevel getRecoveryLevel();

    Optional<Integer> getRecoverableDays();
}
