package com.github.nagyesta.lowkeyvault.service.common;

import com.github.nagyesta.lowkeyvault.service.EntityId;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

@SuppressWarnings("java:S119") //It is easier to ensure that the types are consistent this way
public interface VersionedEntityMultiMap<K extends EntityId, V extends K, RE extends BaseVaultEntity<V>, ME extends RE>
        extends ReadOnlyVersionedEntityMultiMap<K, V, RE> {

    ME getEntity(V entityId);

    void put(V entityId, ME entity);

    boolean isDeleted();

    void moveTo(K entityId, VersionedEntityMultiMap<K, V, RE, ME> destination, UnaryOperator<ME> applyToAll);

    void purgeExpired();

    void purgeDeleted(K entityId);

    void forEachEntity(Consumer<ME> entityConsumer);
}
