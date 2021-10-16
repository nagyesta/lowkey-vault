package com.github.nagyesta.lowkeyvault.service.key;

import com.github.nagyesta.lowkeyvault.service.EntityId;

import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * Base interface of deleted vault entities.
 *
 * @param <V> The type of the versioned Id identifying this entity.
 */
public interface ReadOnlyDeletedEntity<V extends EntityId> {

    V getId();

    Optional<OffsetDateTime> getDeletedDate();

    Optional<OffsetDateTime> getScheduledPurgeDate();
}
