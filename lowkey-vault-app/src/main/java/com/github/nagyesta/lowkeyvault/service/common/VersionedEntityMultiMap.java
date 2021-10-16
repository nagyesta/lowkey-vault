package com.github.nagyesta.lowkeyvault.service.common;

import com.github.nagyesta.lowkeyvault.service.EntityId;

import java.util.function.Function;

public interface VersionedEntityMultiMap<K extends EntityId, V extends K, RE extends BaseVaultEntity<V>, ME extends RE>
        extends ReadOnlyVersionedEntityMultiMap<K, V, RE> {

    ME getEntity(V entityId);

    void put(V entityId, ME entity);

    boolean isDeleted();

    void moveTo(K entityId, VersionedEntityMultiMap<K, V, RE, ME> destination, Function<ME, ME> applyToAll);

    void purgeExpired();
}
