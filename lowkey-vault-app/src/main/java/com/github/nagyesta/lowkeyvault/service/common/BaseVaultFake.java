package com.github.nagyesta.lowkeyvault.service.common;

import com.github.nagyesta.lowkeyvault.service.EntityId;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * The base interface of the vault fakes.
 *
 * @param <K> The type of the key (not versioned).
 * @param <V> The versioned key type.
 * @param <E> The entity type.
 */
public interface BaseVaultFake<K extends EntityId, V extends K, E extends BaseVaultEntity<V>>
        extends TimeAware {

    ReadOnlyVersionedEntityMultiMap<K, V, E> getEntities();

    ReadOnlyVersionedEntityMultiMap<K, V, E> getDeletedEntities();

    void clearTags(V entityId);

    void addTags(V entityId, Map<String, String> tags);

    void setEnabled(V entityId, boolean enabled);

    void setExpiry(V entityId, OffsetDateTime notBefore, OffsetDateTime expiry);

    void delete(K entityId);

    void recover(K entityId);

    void purge(K entityId);

}
